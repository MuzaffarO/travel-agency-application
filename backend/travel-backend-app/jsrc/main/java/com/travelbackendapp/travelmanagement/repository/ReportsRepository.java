package com.travelbackendapp.travelmanagement.repository;

import com.travelbackendapp.travelmanagement.model.entity.ReportRecord;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ReportsRepository {
    
    private final DynamoDbTable<ReportRecord> reportsTable;
    private final String tableName;
    
    @Inject
    public ReportsRepository(DynamoDbClient dynamoDbClient, String reportsTableName) {
        this.tableName = reportsTableName;
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
        
        this.reportsTable = enhancedClient.table(tableName, TableSchema.fromBean(ReportRecord.class));
    }
    
    /**
     * Save a report record to DynamoDB
     * @param reportRecord The report record to save
     * @return The saved report record
     */
    public ReportRecord save(ReportRecord reportRecord) {
        try {
            reportsTable.putItem(reportRecord);
            return reportRecord;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save report record: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find a report record by reportId
     * @param reportId The report ID to search for
     * @return Optional containing the report record if found
     */
    public Optional<ReportRecord> findByReportId(String reportId) {
        try {
            Key key = Key.builder()
                .partitionValue(reportId)
                .build();
            
            ReportRecord reportRecord = reportsTable.getItem(key);
            return Optional.ofNullable(reportRecord);
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find report record with ID: " + reportId, e);
        }
    }
    
    /**
     * Delete a report record by reportId
     * @param reportId The report ID to delete
     * @return true if the record was deleted, false if not found
     */
    public boolean deleteByReportId(String reportId) {
        try {
            Key key = Key.builder()
                .partitionValue(reportId)
                .build();
            
            ReportRecord deletedRecord = reportsTable.deleteItem(key);
            return deletedRecord != null;
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete report record with ID: " + reportId, e);
        }
    }
    
    /**
     * Check if the reports table exists and is accessible
     * @return true if the table is accessible
     */
    public boolean isTableAccessible() {
        try {
            // Try to describe the table to check if it exists and is accessible
            reportsTable.describeTable();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the table name
     * @return The DynamoDB table name
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * Get all report records from the table
     * @return List of all report records
     */
    public List<ReportRecord> findAll() {
        try {
            List<ReportRecord> reports = new ArrayList<>();
            reportsTable.scan().items().forEach(reports::add);
            return reports;
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan all reports: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find all reports by agent email
     * @param agentEmail The agent email to filter by
     * @return List of report records for the agent
     */
    public List<ReportRecord> findByAgentEmail(String agentEmail) {
        try {
            return findAll().stream()
                .filter(report -> agentEmail.equals(report.getAgentEmail()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find reports by agent email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find all reports by tour ID
     * @param tourId The tour ID to filter by
     * @return List of report records for the tour
     */
    public List<ReportRecord> findByTourId(String tourId) {
        try {
            return findAll().stream()
                .filter(report -> tourId.equals(report.getTourId()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find reports by tour ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find all reports by event type
     * @param eventType The event type to filter by (CONFIRM, CANCEL, FINISH)
     * @return List of report records for the event type
     */
    public List<ReportRecord> findByEventType(String eventType) {
        try {
            return findAll().stream()
                .filter(report -> eventType.equals(report.getEventType()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find reports by event type: " + e.getMessage(), e);
        }
    }
}

