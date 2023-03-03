package com.fetch.jake.receiptprocessor.domain;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ReceiptRequestItem {

    @NotNull
    @NotBlank
    String shortDescription;

    // Cap at max int for now: 2^31 -> 2147483648.00
    @NotNull
    @Digits(integer = 10, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    @DecimalMax(value = "2147483647.00", inclusive = true)
    BigDecimal price;

    @Override
    public String toString() {
        return "ReceiptItem [shortDescription=" + shortDescription + ", price=" + price + "]";
    }
}