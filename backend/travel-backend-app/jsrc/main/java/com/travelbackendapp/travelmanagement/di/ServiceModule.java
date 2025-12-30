package com.travelbackendapp.travelmanagement.di;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.repository.*;

import com.travelbackendapp.travelmanagement.service.*;
import com.travelbackendapp.travelmanagement.service.impl.*;
import com.travelbackendapp.travelmanagement.util.S3DocumentsStorage;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validator;

@Module
public class ServiceModule {


    @Provides
    @Singleton
    ToursService provideToursService(ToursRepository toursRepository, ReviewsRepository reviewsRepo,
                                     BookingsRepository bookingsRepo, ObjectMapper mapper, Validator validator,
                                     CognitoIdentityProviderClient cognitoClient,
                                     @Named("userPoolId") String userPoolId,
                                     TravelAgentRepository travelAgentRepository) {
        return new ToursServiceImpl(toursRepository, reviewsRepo, bookingsRepo, mapper, validator, cognitoClient, userPoolId, travelAgentRepository);
    }


    @Provides
    @Singleton
    AuthService provideAuthService(CognitoIdentityProviderClient cognitoClient, 
                                   @Named("userPoolId") String userPoolId,
                                   TravelAgentRepository travelAgentRepository) {
        return new AuthServiceImpl(cognitoClient, userPoolId, travelAgentRepository);
    }

    @Provides
    @Singleton
    public BookingsService provideBookingsService(ObjectMapper mapper, ToursRepository toursRepo, BookingsRepository bookingsRepo, TravelAgentRepository travelAgentRepo,
                                                  BookingsStatusRepository bookingsStatusRepo, BookingEventPublisher eventPublisher,
                                                  S3DocumentsStorage s3DocumentsStorage, DocumentsRepository documentsRepository,
                                                  @Named("BOOKING_DOCS_BUCKET") String bookingDocsBucket,
                                                  @Named("AWS_REGION") String awsRegion) {
        return new BookingsServiceImpl(mapper, toursRepo, bookingsRepo, travelAgentRepo, bookingsStatusRepo, eventPublisher, s3DocumentsStorage, documentsRepository, bookingDocsBucket, awsRegion);
    }

    @Provides
    @Singleton
    public BookingEventPublisher provideBookingEventPublisher(SqsClient sqsClient, 
                                                             @Named("BOOKING_EVENTS_QUEUE_URL") String queueUrl,
                                                             ObjectMapper objectMapper) {
        return new BookingEventPublisher(sqsClient, queueUrl, objectMapper);
    }

    @Provides
    @Named("BOOKING_EVENTS_QUEUE_URL")
    String provideBookingEventsQueueUrl() {
        String queueUrl = System.getenv("BOOKING_EVENTS_QUEUE_URL");
        if (queueUrl == null || queueUrl.isEmpty()) {
            throw new IllegalStateException("Missing env var 'BOOKING_EVENTS_QUEUE_URL'");
        }
        return queueUrl;
    }

    @Provides
    @Singleton
    public ReportsRepository provideReportsRepository(DynamoDbClient dynamoDbClient, 
                                                       @Named("REPORTS_TABLE") String tableName) {
        return new ReportsRepository(dynamoDbClient, tableName);
    }

    @Provides
    @Singleton
    public TravelReportsService provideTravelReportsService(ReportsRepository reportsRepository, ToursRepository toursRepository, ReviewsRepository reviewsRepository) {
        return new TravelReportsServiceImpl(reportsRepository, toursRepository, reviewsRepository);
    }

    @Provides
    @Singleton
    S3DocumentsStorage provideS3DocumentsStorage(
            software.amazon.awssdk.services.s3.S3Client s3,
            @Named("BOOKING_DOCS_BUCKET") String bucket) {
        return new S3DocumentsStorage(s3, bucket);
    }

    @Provides @Singleton
    DocumentsRepository provideDocumentsRepository(software.amazon.awssdk.services.dynamodb.DynamoDbClient dynamoDbClient,
                                                   @Named("documents_table") String tableName) {
        return new DocumentsRepository(dynamoDbClient, tableName);
    }

    @Provides @Singleton
    AiChatService provideAiChatService(ObjectMapper mapper,
                                       ToursRepository toursRepository,
                                       @Named("GEMINI_API_KEY") String apiKey) {
        return new AiChatServiceImpl(mapper, toursRepository, apiKey, provideGeminiModel());
    }

    @Provides @Named("GEMINI_API_KEY")
    String provideGeminiApiKey() {
        String v = System.getenv("GEMINI_API_KEY");
        if (v == null || v.isBlank()) {
            return "no env var set";
        }
        return v;
    }

    @Provides @Named("GEMINI_MODEL")
    String provideGeminiModel() {
        String v = System.getenv("GEMINI_MODEL");
        return (v == null || v.isBlank()) ? "gemini-2.0-flash" : v;
    }

    @Provides
    @Singleton
    TravelAgentsService provideTravelAgentsService(TravelAgentRepository agentsRepo, ObjectMapper mapper,
                                                   Validator validator, CognitoIdentityProviderClient cognitoClient,
                                                   @Named("userPoolId") String userPoolId) {
        return new TravelAgentsServiceImpl(agentsRepo, mapper, validator, cognitoClient, userPoolId);
    }

}
