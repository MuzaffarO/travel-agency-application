package com.travelbackendapp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID;

import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.model.entity.ReportRecord;
import com.travelbackendapp.travelmanagement.model.entity.ReviewItem;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.model.event.BookingEvent;
import com.travelbackendapp.travelmanagement.repository.BookingsRepository;
import com.travelbackendapp.travelmanagement.repository.ReportsRepository;
import com.travelbackendapp.travelmanagement.repository.ReviewsRepository;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;
import com.travelbackendapp.travelmanagement.di.DaggerAppComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${pool_name}")
@DependsOn(resourceType = ResourceType.SQS_QUEUE, name = "${booking_events_queue_url}")
@LambdaHandler(
    lambdaName = "booking-event-handler",
	roleName = "travel-api-handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables({
    @EnvironmentVariable(key = "table_name", value = "${target_table}"),
    @EnvironmentVariable(key = "region", value = "${region}"),
    @EnvironmentVariable(key = "reviews_table", value = "${reviews_table}"),
    @EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${pool_name}", valueTransformer = USER_POOL_NAME_TO_USER_POOL_ID),
    @EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${pool_name}", valueTransformer = USER_POOL_NAME_TO_CLIENT_ID),
    @EnvironmentVariable(key = "travel_agent_table_name", value = "${travel_agent_table_name}"),
    @EnvironmentVariable(key = "bookings_table", value = "${bookings_table}"),
    @EnvironmentVariable(key = "reports_table", value = "${reports_table}")

})
@SqsTriggerEventSource(targetQueue = "${booking_events_queue_url}", batchSize = 1)
public class BookingEventHandler implements RequestHandler<SQSEvent, String> {
    
    private static final Logger log = LoggerFactory.getLogger(BookingEventHandler.class);
    
    @Inject BookingsRepository bookingsRepository;
    @Inject TravelAgentRepository travelAgentRepository;
    @Inject ReportsRepository reportsRepository;
    @Inject ReviewsRepository reviewsRepository;
    @Inject ObjectMapper objectMapper;
    
    public BookingEventHandler() {
        DaggerAppComponent.create().inject(this);
    }
    
    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        int processedCount = 0;
        int errorCount = 0;
        
        try {
            log.info("Processing {} SQS messages", sqsEvent.getRecords().size());
            
            for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
                try {
                    // Parse the booking event from SQS message
                    BookingEvent event = objectMapper.readValue(message.getBody(), BookingEvent.class);
                    
                    log.info("Processing booking event: {} for booking: {}", event.getEventType(), event.getBookingId());
                    
                    // Validate event
                    if (!isValidEvent(event)) {
                        log.error("Invalid booking event received: {}", event);
                        errorCount++;
                        continue;
                    }
                    
                    // Extract and enrich data
                    ReportRecord reportRecord = createReportRecord(event);
                    
                    // Save to reports table
                    reportsRepository.save(reportRecord);
                    
                    log.info("Successfully processed booking event: {} for booking: {}", 
                            event.getEventType(), event.getBookingId());
                    
                    processedCount++;
                    
                } catch (Exception e) {
                    log.error("Error processing SQS message: {}", message.getMessageId(), e);
                    errorCount++;
                }
            }
            
            String result = String.format("Processed: %d, Errors: %d", processedCount, errorCount);
            log.info("SQS event processing completed: {}", result);
            return result;
            
        } catch (Exception e) {
            log.error("Error processing SQS event", e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Validate the incoming booking event
     */
    private boolean isValidEvent(BookingEvent event) {
        if (event == null) {
            log.error("Event is null");
            return false;
        }
        
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            log.error("Event type is null or empty");
            return false;
        }
        
        if (!isValidEventType(event.getEventType())) {
            log.error("Invalid event type: {}", event.getEventType());
            return false;
        }
        
        if (event.getBookingId() == null || event.getBookingId().trim().isEmpty()) {
            log.error("Booking ID is null or empty");
            return false;
        }
        
        if (event.getUserId() == null || event.getUserId().trim().isEmpty()) {
            log.error("User ID is null or empty");
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if the event type is valid
     */
    private boolean isValidEventType(String eventType) {
        return "CONFIRM".equals(eventType) || 
               "CANCEL".equals(eventType) || 
               "FINISH".equals(eventType);
    }
    
    /**
     * Create a comprehensive report record from the booking event
     */
    private ReportRecord createReportRecord(BookingEvent event) {
        // Generate unique report ID
        String reportId = generateReportId(event);
        
        // Create base report record
        ReportRecord reportRecord = new ReportRecord(
            reportId,
            event.getEventType(),
            event.getBookingId(),
            event.getUserId(),
            event.getTourId(),
            event.getAgentEmail(),
            event.getEventTimestamp() != null ? event.getEventTimestamp() : LocalDateTime.now()
        );
        
        // Enrich with booking data
        enrichWithBookingData(reportRecord, event);
        
        // Enrich with agent data
        enrichWithAgentData(reportRecord, event);
        
        // Enrich with feedback data (if available)
        enrichWithFeedbackData(reportRecord, event);
        
        return reportRecord;
    }
    
    /**
     * Generate a unique report ID
     */
    private String generateReportId(BookingEvent event) {
        String timestamp = LocalDateTime.now().toString().replace(":", "-").replace(".", "-");
        return String.format("%s-%s-%s", event.getEventType(), event.getBookingId(), timestamp);
    }
    
    /**
     * Enrich report record with booking data
     */
    private void enrichWithBookingData(ReportRecord reportRecord, BookingEvent event) {
        try {
            // Try to get booking data from repository
            // Note: BookingsRepository doesn't have findByBookingId method
            // We need to scan by userId and find matching bookingId
            List<BookingItem> userBookings = bookingsRepository.findByUserId(event.getUserId());
            BookingItem booking = userBookings.stream()
                .filter(b -> b.getBookingId().equals(event.getBookingId()))
                .findFirst()
                .orElse(null);
            
            if (booking != null) {
                reportRecord.setBookingStatus(booking.getStatus());
                reportRecord.setBookingDate(booking.getCreatedAtEpoch() != null ? 
                    LocalDateTime.ofEpochSecond(booking.getCreatedAtEpoch() / 1000, 0, java.time.ZoneOffset.UTC).toString() : null);
                reportRecord.setTravelDate(booking.getStartDate());
                reportRecord.setNumberOfGuests((booking.getAdults() != null ? booking.getAdults() : 0) + 
                    (booking.getChildren() != null ? booking.getChildren() : 0));
                reportRecord.setTotalPrice(booking.getTotalPrice());
                
                if ("CANCEL".equals(event.getEventType())) {
                    // BookingItem doesn't have cancellationReason field
                    // We could add this field or handle it differently
                    reportRecord.setCancellationReason("Booking cancelled");
                }
            } else {
                // Use data from event if available
                if (event.getBookingData() != null) {
                    BookingEvent.BookingData bookingData = event.getBookingData();
                    reportRecord.setBookingStatus(bookingData.getStatus());
                    reportRecord.setBookingDate(bookingData.getBookingDate() != null ? 
                        bookingData.getBookingDate().toString() : null);
                    reportRecord.setTravelDate(bookingData.getTravelDate() != null ? 
                        bookingData.getTravelDate().toString() : null);
                    reportRecord.setNumberOfGuests(bookingData.getNumberOfGuests());
                    reportRecord.setTotalPrice(bookingData.getTotalPrice());
                    reportRecord.setCancellationReason(bookingData.getCancellationReason());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to enrich with booking data: {}", e.getMessage());
        }
    }
    
    /**
     * Enrich report record with agent data
     */
    private void enrichWithAgentData(ReportRecord reportRecord, BookingEvent event) {
        try {
            if (event.getAgentEmail() != null && !event.getAgentEmail().trim().isEmpty()) {
                // Try to get agent data from repository
                TravelAgent agent = travelAgentRepository.findByEmail(event.getAgentEmail());
                
                if (agent != null) {
                    reportRecord.setAgentName(agent.getFirstName() + " " + agent.getLastName());
                    reportRecord.setAgentRole(agent.getRole());
                } else {
                    // Use data from event if available
                    if (event.getAgentData() != null) {
                        BookingEvent.AgentData agentData = event.getAgentData();
                        reportRecord.setAgentName(agentData.getAgentName());
                        reportRecord.setAgentRole(agentData.getAgentRole());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to enrich with agent data: {}", e.getMessage());
        }
    }
    
    /**
     * Enrich report record with feedback data
     */
    private void enrichWithFeedbackData(ReportRecord reportRecord, BookingEvent event) {
        try {
            // Only enrich with feedback data for FINISH events
            if ("FINISH".equals(event.getEventType()) && event.getTourId() != null) {
                    // If no feedback data in event, fetch customer's rating from reviews table
                    Integer customerRating = getCustomerRatingFromReviews(event.getTourId(), event.getUserId());
                    if (customerRating != null) {
                        reportRecord.setRating(customerRating);
                        reportRecord.setFeedbackDate(LocalDateTime.now().toString());
                        log.debug("Retrieved customer rating {} for tour {} and user {}", 
                                customerRating, event.getTourId(), event.getUserId());
                    } else {
                        log.debug("No customer rating found for tour {} and user {}", 
                                event.getTourId(), event.getUserId());
                    }
                
            }
        } catch (Exception e) {
            log.warn("Failed to enrich with feedback data: {}", e.getMessage());
        }
    }
    
    /**
     * Get customer's rating from reviews table
     */
    private Integer getCustomerRatingFromReviews(String tourId, String userId) {
        try {
            List<ReviewItem> items = reviewsRepository.scanByTourAndAuthor(tourId, userId);
            if (items == null || items.isEmpty()) return null;

            // pick latest by createdAt
            ReviewItem latest = items.stream()
                    .filter(r -> r.getRate() != null)
                    .max(java.util.Comparator.comparing(r -> r.getCreatedAt() == null ? "" : r.getCreatedAt()))
                    .orElse(null);

            return latest != null ? latest.getRate() : null;

        } catch (Exception e) {
            log.warn("Failed to get customer rating for tour {} and user {}: {}", tourId, userId, e.getMessage());
            return null;
        }
    }

}
