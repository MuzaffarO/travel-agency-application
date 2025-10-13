package com.travelbackendapp.travelmanagement.repository;

import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Singleton
public class ToursRepository {

    private static final Logger log = LoggerFactory.getLogger(ToursRepository.class);

    private final DynamoDbTable<TourItem> table;

    @Inject
    public ToursRepository(DynamoDbEnhancedClient client, @Named("TOUR_TABLE") String tableName) {
        this.table = client.table(tableName, TableSchema.fromBean(TourItem.class));
    }

    // ToursRepository.java
    public List<TourItem> findAvailableTours(
            String destination,
            String startDate,
            String endDate,
            String duration,
            List<String> mealPlans,
            List<String> tourTypes,
            Integer reqAdults,
            Integer reqChildren
    ) {
        log.info("findAvailableTours: destination={}, startDate={}, endDate={}, duration={}, mealPlans={}, tourTypes={}, adults={}, children={}",
                destination, startDate, endDate, duration, mealPlans, tourTypes, reqAdults, reqChildren);

        List<TourItem> out = new ArrayList<>();

        try {
            Map<String, AttributeValue> values = new HashMap<>();
            Map<String, String> names = new HashMap<>();
            StringBuilder filter = new StringBuilder();

            // Always require bookable packages + agent email + not expired
            filter.append("#ap > :zero");
            names.put("#ap", "availablePackages");
            values.put(":zero", AttributeValue.builder().n("0").build());

            filter.append(" AND attribute_exists(#ae) AND size(#ae) > :zeroLen");
            names.put("#ae", "agentEmail");
            values.put(":zeroLen", AttributeValue.builder().n("0").build());

            names.put("#sd", "startDate");
            String todayIso = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString();
            filter.append(" AND #sd >= :today");
            values.put(":today", AttributeValue.builder().s(todayIso).build());

            // tourTypes: (#tt = :tt0 OR #tt = :tt1 ...)
            if (tourTypes != null && !tourTypes.isEmpty()) {
                names.put("#tt", "tourType");
                List<String> ors = new ArrayList<>();
                for (int i = 0; i < tourTypes.size(); i++) {
                    String v = ":tt" + i;
                    ors.add("#tt = " + v);
                    values.put(v, AttributeValue.builder().s(tourTypes.get(i)).build());
                }
                filter.append(" AND (").append(String.join(" OR ", ors)).append(")");
            }

            // mealPlans: (contains(#mp,:mp0) OR contains(#mp,:mp1) ...)
            if (mealPlans != null && !mealPlans.isEmpty()) {
                names.put("#mp", "mealPlans");
                List<String> ors = new ArrayList<>();
                for (int i = 0; i < mealPlans.size(); i++) {
                    String v = ":mp" + i;
                    ors.add("contains(#mp, " + v + ")");
                    values.put(v, AttributeValue.builder().s(mealPlans.get(i)).build());
                }
                filter.append(" AND (").append(String.join(" OR ", ors)).append(")");
            }

            // duration (single string still)
            if (duration != null && !duration.isEmpty()) {
                names.put("#dur", "durations");
                filter.append(" AND contains(#dur, :dur)");
                values.put(":dur", AttributeValue.builder().s(duration).build());
            }

            if (reqAdults != null) {
                names.put("#ma", "maxAdults");
                filter.append(" AND (#ma >= :ad)");
                values.put(":ad", AttributeValue.builder().n(String.valueOf(reqAdults)).build());
            }
            if (reqChildren != null) {
                names.put("#mc", "maxChildren");
                filter.append(" AND (#mc >= :ch)");
                values.put(":ch", AttributeValue.builder().n(String.valueOf(reqChildren)).build());
            }

            // Date range bounds
            boolean hasStartBound = startDate != null && !startDate.isEmpty();
            boolean hasEndBound   = endDate != null && !endDate.isEmpty();
            if (hasStartBound) {
                filter.append(" AND #sd >= :sd");
                values.put(":sd", AttributeValue.builder().s(startDate).build());
            }
            if (hasEndBound) {
                filter.append(" AND #sd <= :ed");
                values.put(":ed", AttributeValue.builder().s(endDate).build());
            }

            // Destination
            if (destination != null) {
                String dst = destination.trim();
                if (!dst.isEmpty() && !"any destination".equalsIgnoreCase(dst)) {
                    names.put("#dst", "destination");
                    filter.append(" AND #dst = :dst");
                    values.put(":dst", AttributeValue.builder().s(dst).build());
                }
            }

            Expression filterExpr = Expression.builder()
                    .expression(filter.toString())
                    .expressionValues(values)
                    .expressionNames(names)
                    .build();

            ScanEnhancedRequest req = ScanEnhancedRequest.builder()
                    .filterExpression(filterExpr)
                    .build();

            for (Page<TourItem> p : table.scan(req)) {
                out.addAll(p.items());
            }
            log.info("DDB scan returned {} items", out.size());
        } catch (Exception e) {
            log.error("DDB scan failed", e);
        }

        return out;
    }


    /**
     * Returns unique destination strings that contain the query (case-insensitive), sorted asc, up to `limit`.
     */
    public List<String> findDestinationsLike(String query, int limit) {
        final String needle = query.toLowerCase(Locale.ROOT);
        final Set<String> uniq = new LinkedHashSet<>();

        try {
            ScanEnhancedRequest req = ScanEnhancedRequest.builder()
                    .attributesToProject("destination") // projection
                    .build();

            SdkIterable<Page<TourItem>> pages = table.scan(req);
            for (Page<TourItem> p : pages) {
                for (TourItem it : p.items()) {
                    String dst = it.getDestination();
                    if (dst != null && dst.toLowerCase(Locale.ROOT).contains(needle)) {
                        uniq.add(dst);
                        if (uniq.size() >= limit) break;
                    }
                }
                if (uniq.size() >= limit) break;
            }
        } catch (Exception e) {
            log.error("findDestinationsLike scan failed", e);
        }

        return uniq.stream()
                .sorted(Comparator.comparing(s -> s.toLowerCase(Locale.ROOT)))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Optional<TourItem> getById(String tourId) {
        try {
            TourItem found = table.getItem(Key.builder().partitionValue(tourId).build());
            return Optional.ofNullable(found);
        } catch (Exception e) {
            log.error("getById failed", e);
            return Optional.empty();
        }
    }

    public void applyNewReview(String tourId, int newRate) {
        final int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            TourItem cur = table.getItem(Key.builder().partitionValue(tourId).build());
            if (cur == null) {
                log.warn("applyNewReview: tour not found id={}", tourId);
                return;
            }

            int curCount = cur.getReviews() == null ? 0 : cur.getReviews();
            double curAvg = cur.getRating() == null ? 0.0 : cur.getRating();

            int newCount = curCount + 1;
            double newAvg = ((curAvg * curCount) + newRate) / newCount;
            // round to 2 decimals (optional)
            newAvg = Math.round(newAvg * 100.0) / 100.0;

            cur.setReviews(newCount);
            cur.setRating(newAvg);

            // Only succeed if nobody else updated reviews in the meantime
            Map<String, String> names = Map.of("#rv", "reviews");
            Map<String, AttributeValue> values = Map.of(
                    ":prev", AttributeValue.builder().n(String.valueOf(curCount)).build()
            );
            Expression cond = Expression.builder()
                    .expression("attribute_not_exists(#rv) OR #rv = :prev")
                    .expressionNames(names)
                    .expressionValues(values)
                    .build();

            try {
                table.updateItem(UpdateItemEnhancedRequest.builder(TourItem.class)
                        .item(cur)
                        .conditionExpression(cond)
                        .build());
                return; // success
            } catch (ConditionalCheckFailedException ccfe) {
                log.info("applyNewReview conflict (attempt {}/{}), retrying…", attempt, maxRetries);
            } catch (Exception e) {
                log.error("applyNewReview failed", e);
                return;
            }
        }
        log.warn("applyNewReview: failed after {} retries for {}", 3, tourId);
    }

    public void updateReview(String tourId, int oldRate, int newRate) {
        final int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            TourItem cur = table.getItem(Key.builder().partitionValue(tourId).build());
            if (cur == null) {
                log.warn("applyNewReview: tour not found id={}", tourId);
                return;
            }

            int count = cur.getReviews() == null ? 0 : cur.getReviews();
            double curAvg = cur.getRating() == null ? 0.0 : cur.getRating();

            double newAvg = ((curAvg * count) - oldRate + newRate) / count;
            // round to 2 decimals (optional)
            newAvg = Math.round(newAvg * 100.0) / 100.0;

            cur.setRating(newAvg);

            // Only succeed if nobody else updated reviews in the meantime
            Map<String, String> names = Map.of("#rv", "reviews");
            Map<String, AttributeValue> values = Map.of(
                    ":prev", AttributeValue.builder().n(String.valueOf(count)).build()
            );
            Expression cond = Expression.builder()
                    .expression("attribute_not_exists(#rv) OR #rv = :prev")
                    .expressionNames(names)
                    .expressionValues(values)
                    .build();

            try {
                table.updateItem(UpdateItemEnhancedRequest.builder(TourItem.class)
                        .item(cur)
                        .conditionExpression(cond)
                        .build());
                return; // success
            } catch (ConditionalCheckFailedException ccfe) {
                log.info("applyNewReview conflict (attempt {}/{}), retrying…", attempt, maxRetries);
            } catch (Exception e) {
                log.error("applyNewReview failed", e);
                return;
            }
        }
        log.warn("applyNewReview: failed after {} retries for {}", 3, tourId);
    }

    // repository/ToursRepository.java  (add this method)
    public List<TourItem> listAll() {
        java.util.List<TourItem> out = new java.util.ArrayList<>();
        try {
            for (Page<TourItem> p : table.scan(ScanEnhancedRequest.builder().build())) {
                out.addAll(p.items());
            }
        } catch (Exception e) {
            log.error("listAll scan failed", e);
        }
        return out;
    }

}
