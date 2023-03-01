package com.fetch.jake.receiptprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Receipt {
    String id;
    int points;
    String retailer;
    BigDecimal total;
    LocalDateTime purchaseDateTime;
    List<ReceiptItem> items;

    public Receipt(String id, int points, String retailer, LocalDateTime purchaseDateTime, BigDecimal total,
                   List<ReceiptItem> items) {
        this.id = id;
        this.points = points;
        this.retailer = retailer;
        this.purchaseDateTime = purchaseDateTime;
        this.total = total;
        this.items = items;
    }

    @Override
    public String toString() {
        return "Receipt [id=" + id + ", points=" + points + ", retailer=" + retailer + ", purchaseDateTime="
                + purchaseDateTime + ", total=" + total + ", items=" + items + "]";
    }
}