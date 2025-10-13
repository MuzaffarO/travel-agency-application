package com.travelbackendapp.travelmanagement.di;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dagger.Module;
import dagger.Provides;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class InfraModule {

    @Provides
    @Singleton
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Provides
    @Singleton
    @Named("TOUR_TABLE")
    String tourTableName() {
        String v = System.getenv("table_name");
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Missing env var 'table_name'");
        }
        return v;
    }

    @Provides
    @Singleton
    @Named("TRAVEL_AGENT_TABLE")
    String travelAgentTableName() {
        String v = System.getenv("travel_agent_table_name");
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Missing env var 'travel_agent_table_name'");
        }
        return v;
    }

    @Provides
    @Singleton
    Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    @Provides
    @Singleton
    @Named("REVIEWS_TABLE")
    String reviewsTableName() {
        String v = System.getenv("reviews_table");
        if (v == null || v.isEmpty()) throw new IllegalStateException("Missing env var 'reviews_table'");
        return v;
    }

    @Provides
    @Singleton
    @Named("BOOKINGS_TABLE")
    String bookingsTableName() {
        String v = System.getenv("bookings_table");
        if (v == null || v.isEmpty()) throw new IllegalStateException("Missing env var 'bookings_table'");
        return v;
    }

    @Provides
    @Singleton
    @Named("REPORTS_TABLE")
    String reportsTableName() {
        String v = System.getenv("reports_table");
        if (v == null || v.isEmpty()) throw new IllegalStateException("Missing env var 'reports_table'");
        return v;
    }

    @Provides @Singleton @Named("BOOKING_DOCS_BUCKET")
    String bookingDocsBucket() {
        String v = System.getenv("BOOKING_DOCS_BUCKET");
        if (v == null || v.isEmpty()) throw new IllegalStateException("Missing env var 'BOOKING_DOCS_BUCKET'");
        return v;
    }

    @Provides @Singleton @Named("documents_table")
    String documentsTableName() {
        String v = System.getenv("documents_table");
        if (v == null || v.isEmpty()) throw new IllegalStateException("Missing env var 'documents_table'");
        return v;
    }

    @Provides @Singleton @Named("AWS_REGION")
    String awsRegion() {
        String v = System.getenv("region");
        if (v == null || v.isEmpty()) throw new IllegalStateException("Missing env var 'region'");
        return v;
    }






}
