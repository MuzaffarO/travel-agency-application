package com.travelbackendapp.travelmanagement.repository;

import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.service.impl.BookingsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class TravelAgentRepository {

    private static final Logger log = LoggerFactory.getLogger(TravelAgentRepository.class);
    private final DynamoDbTable<TravelAgent> travelAgentTable;

    @Inject
    public TravelAgentRepository(DynamoDbEnhancedClient enhancedClient, 
                                @Named("TRAVEL_AGENT_TABLE") String tableName) {
        this.travelAgentTable = enhancedClient.table(tableName, TableSchema.fromBean(TravelAgent.class));
    }

    /**
     * Check if an email exists in the Travel Agent list
     * @param email The email to check
     * @return TravelAgent object if found, null otherwise
     */
    public TravelAgent findByEmail(String email) {
        try {
            Key key = Key.builder()
                    .partitionValue(email)
                    .build();
            
            return travelAgentTable.getItem(key);
        } catch (ResourceNotFoundException e) {
            log.info(e.getMessage());
            return null;
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    /**
     * Save a new Travel Agent to the database
     * @param travelAgent The TravelAgent object to save
     * @return The saved TravelAgent object
     */
    public TravelAgent save(TravelAgent travelAgent) {
        travelAgentTable.putItem(travelAgent);
        return travelAgent;
    }

    /**
     * Delete a Travel Agent from the database
     * @param email The email of the Travel Agent to delete
     */
    public void deleteByEmail(String email) {
        Key key = Key.builder()
                .partitionValue(email)
                .build();
        
        travelAgentTable.deleteItem(key);
    }
}
