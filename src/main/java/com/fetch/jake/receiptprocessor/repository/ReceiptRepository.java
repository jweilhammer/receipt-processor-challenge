package com.fetch.jake.receiptprocessor.repository;

import com.fetch.jake.receiptprocessor.model.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Slf4j
@Repository
public class ReceiptRepository {

    @Autowired
    private DynamoDbTable<Receipt> dynamoTable;

    public void saveReceipt(Receipt receipt) {
        log.info("Saving receipt: " + receipt);
        dynamoTable.putItem(receipt);
    }

    public Receipt getReceipt(String id) {
        log.info("Retrieving receipt with id: " + id);
        return dynamoTable.getItem(Key.builder().partitionValue(id).build());
    }

    public void deleteReceipt(String id) {
        log.info("DELETING Receipt WITH ID: " + id);
        dynamoTable.deleteItem(Key.builder().partitionValue(id).build());
    }
}
