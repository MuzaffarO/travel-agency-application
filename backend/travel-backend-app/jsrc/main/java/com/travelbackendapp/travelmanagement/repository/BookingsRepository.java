package com.travelbackendapp.travelmanagement.repository;

import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class BookingsRepository {
    private static final Logger log = LoggerFactory.getLogger(BookingsRepository.class);

    private final DynamoDbEnhancedClient enhanced;
    private final DynamoDbTable<BookingItem> bookingTable;
    private final DynamoDbTable<TourItem> toursTable;
    private final DynamoDbClient lowLevel;

    @Inject
    public BookingsRepository(DynamoDbEnhancedClient enhanced,
                              DynamoDbClient lowLevel,
                              @Named("BOOKINGS_TABLE") String bookingsTableName,
                              @Named("TOUR_TABLE") String toursTableName) {
        this.enhanced = enhanced;
        this.lowLevel = lowLevel;
        this.bookingTable = enhanced.table(bookingsTableName, TableSchema.fromBean(BookingItem.class));
        this.toursTable = enhanced.table(toursTableName, TableSchema.fromBean(TourItem.class));
    }

    /**
     * Atomically decrements availablePackages and writes the booking in a single transaction.
     * Fails if the tour has no capacity left or if capacity changed concurrently.
     */
// BookingsRepository.java
    public void transactReserveSeatsAndSave(BookingItem booking, String tourId, int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("seats must be > 0");
        }

        // 1) Read current capacity
        TourItem current = toursTable.getItem(r -> r.key(k -> k.partitionValue(tourId)));
        if (current == null) {
            throw new IllegalStateException("tour not found: " + tourId);
        }
        int cur = current.getAvailablePackages() == null ? 0 : current.getAvailablePackages();
        if (cur < seats) {
            // mirror transaction-style failure so caller can map to 409
            throw software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
                    .builder().message("not enough capacity").build();
        }

        // 2) Build partial item with the new capacity
        TourItem updated = new TourItem();
        updated.setTourId(tourId);
        updated.setAvailablePackages(cur - seats);

        // 3) Condition: value didn’t change and still covers the requested seats
        Expression cond = Expression.builder()
                .expression("#ap = :prev AND #ap >= :seats")
                .expressionNames(java.util.Map.of("#ap", "availablePackages"))
                .expressionValues(java.util.Map.of(
                        ":prev", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().n(String.valueOf(cur)).build(),
                        ":seats", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().n(String.valueOf(seats)).build()
                ))
                .build();

        var updateReq = software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest
                .builder(TourItem.class)
                .ignoreNulls(true)
                .item(updated)
                .conditionExpression(cond)
                .build();

        var tx = software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest
                .builder()
                .addUpdateItem(toursTable, updateReq)
                .addPutItem(bookingTable, booking)
                .build();

        // 4) Execute
        enhanced.transactWriteItems(tx);
    }
    public BookingItem get(String userId, String bookingId) {
        return bookingTable.getItem(Key.builder()
                .partitionValue(userId)
                .sortValue(bookingId)
                .build());
    }

    public void put(BookingItem booking) {
        bookingTable.putItem(booking);
    }

    public void adjustTourCapacity(String tourId, int delta) {
        if (delta == 0) return;

        // Key
        Map<String, AttributeValue> key = Map.of(
                "tourId", AttributeValue.builder().s(tourId).build()
        );

        Map<String, String> names = Map.of("#ap", "availablePackages");

        if (delta > 0) {
            // Consume seats: availablePackages = availablePackages - :d
            int d = delta;

            Map<String, AttributeValue> values = Map.of(
                    ":d", AttributeValue.builder().n(Integer.toString(d)).build()
            );

            lowLevel.updateItem(UpdateItemRequest.builder()
                    .tableName(toursTable.tableName())
                    .key(key)
                    .updateExpression("SET #ap = #ap - :d")
                    // Guard: cannot consume below zero
                    .conditionExpression("#ap >= :d")
                    .expressionAttributeNames(names)
                    .expressionAttributeValues(values)
                    .returnValues(ReturnValue.NONE)
                    .build());

        } else {
            // Return seats: availablePackages = if_not_exists(availablePackages, 0) + :inc
            int inc = -delta;

            Map<String, AttributeValue> values = Map.of(
                    ":inc", AttributeValue.builder().n(Integer.toString(inc)).build(),
                    ":zero", AttributeValue.builder().n("0").build()
            );

            lowLevel.updateItem(UpdateItemRequest.builder()
                    .tableName(toursTable.tableName())
                    .key(key)
                    .updateExpression("SET #ap = if_not_exists(#ap, :zero) + :inc")
                    .expressionAttributeNames(names)
                    .expressionAttributeValues(values)
                    .returnValues(ReturnValue.NONE)
                    .build());
        }
    }

    // Query by PK (userId) — efficient
    public List<BookingItem> findByUserId(String userId) {
        var out = new ArrayList<BookingItem>();
        bookingTable.query(r -> r.queryConditional(
                        QueryConditional.keyEqualTo(k -> k.partitionValue(userId))))
                .items()
                .forEach(out::add);
        return out;
    }

    public List<BookingItem> findByAgentEmail(String email) {
        Expression cond = Expression.builder()
                .expression("agentEmail = :email")
                .expressionValues(Map.of(":email", AttributeValue.fromS(email)))
                .build();

        var scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(cond)
                .build();

        var out = new ArrayList<BookingItem>();
        bookingTable.scan(scanRequest).items().forEach(out::add);
        return out;
    }

    public List<BookingItem> findAll() {
        var out = new ArrayList<BookingItem>();
        try {
            for (Page<BookingItem> page : bookingTable.scan()) {
                out.addAll(page.items());
            }
            log.info("Found {} bookings", out.size());
        } catch (Exception e) {
            log.error("Failed to list all bookings", e);
        }
        return out;
    }

    public BookingItem getByBookingId(String bookingId) {
        var req = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression("#bid = :bid")
                        .expressionNames(Map.of("#bid", "bookingId"))
                        .expressionValues(Map.of(":bid", AttributeValue.builder().s(bookingId).build()))
                        .build())
                .limit(1)
                .build();
        for (Page<BookingItem> p : bookingTable.scan(req)) {
            for (BookingItem it : p.items()) return it;
        }
        return null;
    }

    public List<String> findBookingIdsByTourId(String tourId) {
        List<String> out = new ArrayList<>();
        try {
            Expression filter = Expression.builder()
                    .expression("#tid = :tid")
                    .expressionNames(Map.of("#tid", "tourId"))
                    .expressionValues(Map.of(":tid", AttributeValue.builder().s(tourId).build()))
                    .build();

            // Project only bookingId to reduce read cost
            ScanEnhancedRequest req = ScanEnhancedRequest.builder()
                    .filterExpression(filter)
                    .attributesToProject("bookingId")
                    .build();

            for (Page<BookingItem> p : bookingTable.scan(req)) {
                for (BookingItem b : p.items()) {
                    if (b.getBookingId() != null) out.add(b.getBookingId());
                }
            }
        } catch (Exception e) {
            log.error("findBookingIdsByTourId scan failed for {}", tourId, e);
        }
        return out;
    }


}
