package com.travelbackendapp.travelmanagement.repository;

import com.travelbackendapp.travelmanagement.model.entity.ReviewItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ReviewsRepository {
    private static final Logger log = LoggerFactory.getLogger(ReviewsRepository.class);

    private final DynamoDbTable<ReviewItem> table;

    @Inject
    public ReviewsRepository(DynamoDbEnhancedClient client, @Named("REVIEWS_TABLE") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(ReviewItem.class));
    }

    public List<ReviewItem> scanByTourId(String tourId) {
        var out = new ArrayList<ReviewItem>();
        try {
            var expr = software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("#tid = :tid")
                    .expressionNames(java.util.Map.of("#tid", "tourId"))
                    .expressionValues(java.util.Map.of(":tid", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(tourId).build()))
                    .build();

            var req = software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest.builder()
                    .filterExpression(expr)
                    .build();

            table.scan(req).items().forEach(out::add);
        } catch (Exception e) {
            log.error("scanByTourId failed for {}", tourId, e);
        }
        return out;
    }


    public ReviewItem getByBookingId(String bookingId) {
        try {
            return table.getItem(Key.builder().partitionValue(bookingId).build());
        } catch (Exception e) {
            log.error("getByBookingId failed for {}", bookingId, e);
            return null;
        }
    }

    public void put(ReviewItem item) { table.putItem(item); }

    public void update(ReviewItem item) { table.updateItem(item); }

    public void deleteByBookingId(String bookingId) {
        try {
            table.deleteItem(Key.builder().partitionValue(bookingId).build());
        } catch (Exception e) {
            log.error("deleteByBookingId failed for {}", bookingId, e);
        }
    }
    public List<ReviewItem> scanByTourAndAuthor(String tourId, String authorId) {
        var out = new ArrayList<ReviewItem>();
        try {
            var expr = software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                    .expression("#tid = :tid AND #aid = :aid")
                    .expressionNames(java.util.Map.of("#tid", "tourId", "#aid", "authorId"))
                    .expressionValues(java.util.Map.of(
                            ":tid", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(tourId).build(),
                            ":aid", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(authorId).build()
                    ))
                    .build();

            var req = software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest.builder()
                    .filterExpression(expr)
                    .build();

            table.scan(req).items().forEach(out::add);
        } catch (Exception e) {
            log.error("scanByTourAndAuthor failed for tourId={}, authorId={}", tourId, authorId, e);
        }
        return out;
    }

}
