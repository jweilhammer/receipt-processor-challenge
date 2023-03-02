package com.fetch.jake.receiptprocessor.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    String id;
    int points;
    String retailer;
    BigDecimal total;
    LocalDateTime purchaseDateTime;
    List<ReceiptItem> items;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Receipt [id=" + id + ", points=" + points + ", retailer=" + retailer + ", purchaseDateTime="
                + purchaseDateTime + ", total=" + total + ", items=" + items + "]";
    }

    public void addPoints(int points) {
        this.points += points;
    }
}