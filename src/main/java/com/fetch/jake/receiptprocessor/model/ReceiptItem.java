package com.fetch.jake.receiptprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.math.BigDecimal;

@Getter
@Setter
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItem {
    String shortDescription;
    BigDecimal price;

    @Override
    public String toString() {
        return "ReceiptItem [shortDescription=" + shortDescription + ", price=" + price + "]";
    }

}