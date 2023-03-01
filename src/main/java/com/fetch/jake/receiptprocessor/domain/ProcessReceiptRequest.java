package com.fetch.jake.receiptprocessor.domain;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProcessReceiptRequest {
    @NotBlank
    String retailer;

    @DateTimeFormat(pattern = "YYYY:MM:dd")
    LocalDate purchaseDate;

    @DateTimeFormat(pattern = "HH:mm")
    LocalTime purchaseTime;

    // Cap at max int for now: 2^31 -> 2147483648.00
    @Digits(integer = 10, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    @DecimalMax(value = "2147483647.00", inclusive = true)
    BigDecimal total;

    @Size(min = 1)
    List<ReceiptRequestItem> items;

    @Override
    public String toString() {
        return "ProcessReceiptRequest{" +
                "retailer='" + retailer + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", purchaseTime=" + purchaseTime +
                ", total=" + total +
                ", items=" + items +
                '}';
    }

}