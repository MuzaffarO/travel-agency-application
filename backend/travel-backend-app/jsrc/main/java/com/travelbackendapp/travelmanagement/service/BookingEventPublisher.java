package com.travelbackendapp.travelmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.event.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.LocalDateTime;

@Singleton
public class BookingEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);
    
    private final SqsClient sqsClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;
    
    @Inject
    public BookingEventPublisher(SqsClient sqsClient, 
                                @Named("BOOKING_EVENTS_QUEUE_URL") String queueUrl,
                                ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publish a booking event to SQS queue
     * @param eventType The type of event (CONFIRM, CANCEL, FINISH)
     * @param bookingId The booking ID
     * @param userId The user ID
     * @param tourId The tour ID
     * @param agentEmail The agent email
     */
    public void publishBookingEvent(String eventType, String bookingId, String userId, 
                                  String tourId, String agentEmail) {
        try {
            BookingEvent event = new BookingEvent();
            event.setEventType(eventType);
            event.setBookingId(bookingId);
            event.setUserId(userId);
            event.setTourId(tourId);
            event.setAgentEmail(agentEmail);
            event.setEventTimestamp(LocalDateTime.now());
            
            publishBookingEvent(event);
            
        } catch (Exception e) {
            log.error("Failed to create booking event for {}: {}", eventType, e.getMessage(), e);
        }
    }
    
    /**
     * Publish a booking event to SQS queue
     * @param event The booking event to publish
     */
    public void publishBookingEvent(BookingEvent event) {
        try {
            // Convert event to JSON
            String eventJson = objectMapper.writeValueAsString(event);
            
            // Create SQS message request
            SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(eventJson)
                .messageAttributes(java.util.Map.of(
                    "eventType", software.amazon.awssdk.services.sqs.model.MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(event.getEventType())
                        .build(),
                    "bookingId", software.amazon.awssdk.services.sqs.model.MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(event.getBookingId())
                        .build(),
                    "timestamp", software.amazon.awssdk.services.sqs.model.MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(LocalDateTime.now().toString())
                        .build()
                ))
                .build();
            
            // Send message to SQS
            SendMessageResponse response = sqsClient.sendMessage(request);
            
            log.info("Successfully published booking event {} for booking {} to SQS. MessageId: {}", 
                    event.getEventType(), event.getBookingId(), response.messageId());
            
        } catch (Exception e) {
            log.error("Failed to publish booking event {} for booking {} to SQS: {}", 
                     event.getEventType(), event.getBookingId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish booking event to SQS", e);
        }
    }
    
    /**
     * Publish a booking event with additional data
     * @param eventType The type of event
     * @param bookingId The booking ID
     * @param userId The user ID
     * @param tourId The tour ID
     * @param agentEmail The agent email
     * @param bookingData Additional booking data
     * @param agentData Additional agent data
     * @param feedbackData Additional feedback data
     */
    public void publishBookingEventWithData(String eventType, String bookingId, String userId, 
                                          String tourId, String agentEmail,
                                          BookingEvent.BookingData bookingData,
                                          BookingEvent.AgentData agentData,
                                          BookingEvent.FeedbackData feedbackData) {
        try {
            BookingEvent event = new BookingEvent();
            event.setEventType(eventType);
            event.setBookingId(bookingId);
            event.setUserId(userId);
            event.setTourId(tourId);
            event.setAgentEmail(agentEmail);
            event.setEventTimestamp(LocalDateTime.now());
            event.setBookingData(bookingData);
            event.setAgentData(agentData);
            event.setFeedbackData(feedbackData);
            
            publishBookingEvent(event);
            
        } catch (Exception e) {
            log.error("Failed to create booking event with data for {}: {}", eventType, e.getMessage(), e);
        }
    }
}

