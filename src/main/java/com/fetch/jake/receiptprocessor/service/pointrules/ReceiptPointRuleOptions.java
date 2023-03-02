package com.fetch.jake.receiptprocessor.service.pointrules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptPointRuleOptions {

    // Constants to use as point rule defaults:
    // Regex pattern to match any non-alphanumerical chars
    public static final String NON_ALPHA_NUMERIC_WHITESPACE_REGEX = "[^a-zA-Z\\d]+";

    BigDecimal itemDescriptionPointMultiplier = BigDecimal.valueOf(0.20);

    LocalTime purchaseThresholdStart = LocalTime.of(14, 00);

    // Should be greater than threshold start in current day, no inverse thresholds across multiple days
    LocalTime purchaseThresholdEnd = LocalTime.of(16, 00);
    int itemDescriptionLengthMultiple = 3;
    int itemGroupSize = 2;

    int retailerNameCharCountMultiplier = 1;
    int quarterMultiplePoints = 25;
    int oddPurchaseDayPoints = 6;
    int itemGroupPointCount = 5;
    int purchaseTimeThresholdPoints = 10;
    int evenTotalPoints = 50;
}
