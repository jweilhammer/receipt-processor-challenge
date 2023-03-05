package com.fetch.jake.receiptprocessor.repository;

import com.fetch.jake.receiptprocessor.model.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.net.URI;

/**
 * General configuration for creating DynamoDB Client and table mapper
 */
@Slf4j
@Configuration
public class DynamoDBConfig {

    @Value("${aws.dynamodb.tablename}")
    private String dynamoTableName;

    @Value("${aws.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${aws.secretkey}")
    private String amazonAWSSecretKey;

    @Value("${aws.region}")
    private String awsRegion;

    /*
     * Create mapped table to perform CRUD operations with our defined dynamo pojo
     * If table does not exist, it should be created if on localhost
     */
    @Bean
    @DependsOn({"dynamoDbEnhancedClient"})
    public DynamoDbTable<Receipt> dynamoTable(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        log.info("Initializing DynamoDB mapper for table: " + dynamoTableName + " -> " + Receipt.class);
        var ReceiptTable = dynamoDbEnhancedClient.table(dynamoTableName, TableSchema.fromBean(Receipt.class));

        try {
            // Check existence of table and that we can access it
            log.info("Connected to: " + ReceiptTable.describeTable());
        } catch (ResourceNotFoundException rnf) {
            // Create table locally for development environment / integration tests
            log.error(rnf.getMessage());
            if (amazonDynamoDBEndpoint.contains("localhost") || System.getProperty("integration.tests") != null) {
                log.info("Creating table " + dynamoTableName + " locally with definition defined in: " + Receipt.class);
                ReceiptTable.createTable();
            } else {
                throw rnf;
            }
        } catch (Exception e) {
            // Prevent app startup on any errors connecting to DynamoDB
            log.error("Unable to connect to DynamoDB: ", e);
            throw e;
        }

        return ReceiptTable;
    }

    @Bean
    @DependsOn({"dynamoDbClient"})
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        log.info("Initializing DynamoDB client for endpoint: " + amazonDynamoDBEndpoint + ", region: " + awsRegion);
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(amazonDynamoDBEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(amazonAWSAccessKey, amazonAWSSecretKey)))
                .region(Region.of(awsRegion))
                .build();
    }
}