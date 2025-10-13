package com.travelbackendapp.travelmanagement.repository;

import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.domain.BookingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class BookingsStatusRepository {
    private static final Logger log = LoggerFactory.getLogger(BookingsStatusRepository.class);

    private final DynamoDbTable<BookingItem> bookingTable;

    @Inject
    public BookingsStatusRepository(DynamoDbEnhancedClient enhanced,
                                    @Named("BOOKINGS_TABLE") String bookingsTableName) {
        this.bookingTable = enhanced.table(bookingsTableName, TableSchema.fromBean(BookingItem.class));
    }

    /**
     * Stream only fields we need; skip FINISHED and CANCELLED.
     * We still want BOOKED (candidate for STARTED) and STARTED (candidate for FINISHED).
     */
    public SdkIterable<Page<BookingItem>> scanActiveForStatusUpdate() {
        var names = Map.of(
                "#st", "status",
                "#dur", "duration"
        );
        var values = Map.of(
                ":fin", AttributeValue.builder().s(BookingStatus.FINISHED.name()).build(),
                ":can", AttributeValue.builder().s(BookingStatus.CANCELLED.name()).build()
        );

        var filter = Expression.builder()
                .expression("attribute_exists(startDate) AND attribute_exists(#dur) " +
                        "AND (#st <> :fin) AND (#st <> :can)")
                .expressionNames(names)
                .expressionValues(values)
                .build();

        var req = ScanEnhancedRequest.builder()
                .attributesToProject("userId", "bookingId", "startDate", "duration", "status")
                .filterExpression(filter)
                .build();

        return bookingTable.scan(req);
    }

    /** Set status = STARTED only if currently BOOKED (idempotent). */
    public void markStarted(String userId, String bookingId) {
        BookingItem item = new BookingItem();
        item.setUserId(userId);
        item.setBookingId(bookingId);
        item.setStatus(BookingStatus.STARTED.name());

        var names = Map.of("#st", "status");
        var vals  = Map.of(":cur", AttributeValue.builder().s(BookingStatus.BOOKED.name()).build());

        var condition = Expression.builder()
                .expression("#st = :cur")   // only transition BOOKED -> STARTED
                .expressionNames(names)
                .expressionValues(vals)
                .build();

        bookingTable.updateItem(UpdateItemEnhancedRequest.builder(BookingItem.class)
                .item(item)
                .ignoreNulls(true)
                .conditionExpression(condition)
                .build());
    }

    /** Set status = FINISHED if not already FINISHED (idempotent, conditional). */
    public void markFinished(String userId, String bookingId) {
        BookingItem item = new BookingItem();
        item.setUserId(userId);
        item.setBookingId(bookingId);
        item.setStatus(BookingStatus.FINISHED.name());

        var names = Map.of("#st", "status");
        var vals  = Map.of(":fin", AttributeValue.builder().s(BookingStatus.FINISHED.name()).build());

        var condition = Expression.builder()
                .expression("attribute_not_exists(#st) OR #st <> :fin")
                .expressionNames(names)
                .expressionValues(vals)
                .build();

        bookingTable.updateItem(UpdateItemEnhancedRequest.builder(BookingItem.class)
                .item(item)
                .ignoreNulls(true)
                .conditionExpression(condition)
                .build());
    }

    /** Confirm only if currently BOOKED and assigned to this agent. */
    public void markConfirmed(String userId, String bookingId, String agentEmail) {
        BookingItem patch = new BookingItem();
        patch.setUserId(userId);
        patch.setBookingId(bookingId);
        patch.setStatus(BookingStatus.CONFIRMED.name());
        patch.setConfirmedAtEpoch(System.currentTimeMillis());

        var names = Map.of(
                "#st", "status",
                "#ae", "agentEmail"
        );
        var values = Map.of(
                ":booked", AttributeValue.builder().s(BookingStatus.BOOKED.name()).build(),
                ":agent",  AttributeValue.builder().s(agentEmail).build()
        );

        var cond = Expression.builder()
                .expression("#st = :booked AND #ae = :agent")
                .expressionNames(names)
                .expressionValues(values)
                .build();

        bookingTable.updateItem(UpdateItemEnhancedRequest.builder(BookingItem.class)
                .item(patch)
                .ignoreNulls(true)
                .conditionExpression(cond)
                .build());
    }
}
