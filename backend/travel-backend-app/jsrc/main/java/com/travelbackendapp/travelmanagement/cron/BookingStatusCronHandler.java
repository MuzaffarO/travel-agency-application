package com.travelbackendapp.travelmanagement.cron;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.travelbackendapp.travelmanagement.di.DaggerAppComponent;
import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.repository.BookingsStatusRepository;
import com.travelbackendapp.travelmanagement.service.BookingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;

@LambdaHandler(
        lambdaName = "booking-status-updater",
        roleName = "travel-api-handler-role",
        memory = 512,
        timeout = 60
)
@EnvironmentVariables({
        @EnvironmentVariable(key = "table_name", value = "${target_table}"),
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "bookings_table", value = "${bookings_table}"),
        @EnvironmentVariable(key = "BOOKING_EVENTS_QUEUE_URL", value = "${booking_events_queue_url}")
})
public class BookingStatusCronHandler implements RequestHandler<Object, String> {
    private static final Logger log = LoggerFactory.getLogger(BookingStatusCronHandler.class);

    @Inject BookingsStatusRepository bookingsRepo;
    @Inject BookingEventPublisher eventPublisher;

    public BookingStatusCronHandler() {
        DaggerAppComponent.create().inject(this);
    }

    @Override
    public String handleRequest(Object input, Context context) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int scanned = 0;
        int setFinished = 0;
        int setStarted  = 0;

        for (Page<BookingItem> page : bookingsRepo.scanActiveForStatusUpdate()) {
            for (BookingItem b : page.items()) {
                scanned++;
                try {
                    if (shouldBeFinished(b, today)) {
                        bookingsRepo.markFinished(b.getUserId(), b.getBookingId());
                        setFinished++;
                        
                        // Publish FINISH event to SQS
                        try {
                            eventPublisher.publishBookingEvent(
                                "FINISH",
                                b.getBookingId(),
                                b.getUserId(),
                                b.getTourId(),
                                b.getAgentEmail()
                            );
                            log.info("Published FINISH event for booking {}", b.getBookingId());
                        } catch (Exception e) {
                            log.error("Failed to publish FINISH event for booking {}: {}", b.getBookingId(), e.getMessage(), e);
                        }
                        
                    } else if (shouldBeStarted(b, today)) {
                        bookingsRepo.markStarted(b.getUserId(), b.getBookingId());
                        setStarted++;
                        
                        // Note: CONFIRM events are now published directly from BookingsServiceImpl.confirm()
                        // when travel agents manually confirm bookings
                    }
                } catch (Exception ex) {
                    log.warn("Skip booking userId={} bookingId={} due to error: {}",
                            b.getUserId(), b.getBookingId(), ex.toString());
                }
            }
        }
        String result = String.format(Locale.ROOT,
                "{\"checked\":%d,\"setStarted\":%d,\"setFinished\":%d,\"date\":\"%s\"}",
                scanned, setStarted, setFinished, today);
        log.info("Status cron result {}", result);
        return result;
    }

    /** Start <= today <= End  ⇒ STARTED (if not finished/canceled). */
    private static boolean shouldBeStarted(BookingItem b, LocalDate today) {
        if (b.getStartDate() == null || b.getDuration() == null) return false;
        LocalDate start = LocalDate.parse(b.getStartDate());
        int days = parseDays(b.getDuration());
        if (days <= 0) return false;
        LocalDate endInclusive = start.plusDays(days - 1);
        return ( !today.isAfter(endInclusive) ) && ( !today.isBefore(start) );
    }

    /** today > End  ⇒ FINISHED. */
    private static boolean shouldBeFinished(BookingItem b, LocalDate today) {
        if (b.getStartDate() == null || b.getDuration() == null) return false;
        LocalDate start = LocalDate.parse(b.getStartDate());
        int days = parseDays(b.getDuration());
        if (days <= 0) return false;
        LocalDate end = start.plusDays(days - 1);
        return today.isAfter(end);
    }

    /** Extract integer day count from strings like "7 days". */
    private static int parseDays(String durationStr) {
        if (durationStr == null) return -1;
        String s = durationStr.trim().toLowerCase(Locale.ROOT);
        int i = 0; while (i < s.length() && !Character.isDigit(s.charAt(i))) i++;
        int j = i; while (j < s.length() && Character.isDigit(s.charAt(j))) j++;
        if (i < j) {
            try { return Integer.parseInt(s.substring(i, j)); } catch (NumberFormatException ignored) {}
        }
        return -1;
    }
}
