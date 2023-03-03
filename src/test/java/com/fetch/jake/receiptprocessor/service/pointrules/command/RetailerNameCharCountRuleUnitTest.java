package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.model.ReceiptItem;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class RetailerNameCharCountRuleUnitTest {

    Receipt receipt;
    ReceiptPointRuleOptions options;
    ReceiptPointRuleCommand retailerCharCountRule;

    static String nonAlphaNumericalChars = "~`!@#$%^&*()-_=+{}[];:'\\|/?,.<>";

    @BeforeEach
    void setup() {
        receipt = spy(new Receipt("id", 0, "retailer", BigDecimal.ONE, LocalDateTime.now(),
                List.of(new ReceiptItem("shortDescription", BigDecimal.ONE))));
        options = new ReceiptPointRuleOptions();
        retailerCharCountRule = new RetailerNameCharCountRule(receipt, options);
    }

    @ParameterizedTest
    @MethodSource(value = "roundTotalInputs")
    void countsAlphaNumericalOnly(String retailerName, int expectedCharCount) {
        // setup
        receipt.setRetailer(retailerName);
        receipt.setPoints(0);
        int additionalPoints = expectedCharCount * options.getRetailerNameCharCountMultiplier();

        // when
        retailerCharCountRule.applyRule();

        // then
        assertEquals(receipt.getPoints(), additionalPoints);
        verify(receipt, times(1)).addPoints(additionalPoints);
    }

    public static List<Arguments> roundTotalInputs() {

        // Test case names with varying alphanumeric
        Map<String, Integer> namePoints = new LinkedHashMap<String, Integer>(Map.of(
                " ", 0,
                "!", 0,
                "Walgreens", 9,
                "Walgreens123", 12,
                "123Walgreens", 12,
                "Wal123greens", 12,
                "123Walgreens456", 15,
                "123Wal456greens", 15,
                "123Wal456greens7890", 19,
                "0987654321Wal1234567890greens0987654321", 39)
        );

        // For every input, create 4 test cases where non-alphanumeric chars are inserted somewhere into string
        Map<String, Integer> nonAlphaNumericCombinations = new LinkedHashMap<>();
        namePoints.entrySet().forEach(
                entry -> {
                    String retailer = entry.getKey();
                    nonAlphaNumericCombinations.put(retailer, entry.getValue());
                    nonAlphaNumericCombinations.put(nonAlphaNumericalChars + retailer, entry.getValue());
                    nonAlphaNumericCombinations.put(retailer + nonAlphaNumericalChars, entry.getValue());
                    nonAlphaNumericCombinations.put(retailer.substring(0, Math.min(2, retailer.length())) +
                            nonAlphaNumericalChars +
                            retailer.substring(Math.min(2, retailer.length()), retailer.length()), entry.getValue());
                }
        );

        return nonAlphaNumericCombinations.entrySet()
                .stream()
                .map(entry -> {
                    return Arguments.of(entry.getKey(), entry.getValue());
                })
                .toList();
    }
}