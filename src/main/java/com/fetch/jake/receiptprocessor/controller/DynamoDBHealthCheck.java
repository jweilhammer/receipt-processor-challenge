package com.fetch.jake.receiptprocessor.controller;

import com.fetch.jake.receiptprocessor.model.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * Custom health check accessed on "/actuator/health"
 * <p>
 * <p>
 * Returns { status: UP}, { status: DOWN }
 */
@Component
public class DynamoDBHealthCheck implements HealthIndicator {
    
    @Autowired
    DynamoDbTable<Receipt> dynamoDbTable;

    @Override
    public Health health() {
        try {
            dynamoDbTable.describeTable();
        } catch (ResourceNotFoundException rnf) {
            return Health.down().withDetail("DynamoDB table does not exist", rnf).build();
        } catch (Exception e) {
            return Health.down().withDetail("DynamoDB unaccessible", e).build();
        }

        return Health.up().build();
    }
}