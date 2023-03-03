package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.model.ReceiptItem;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// TODO: add tests for other multiples / multipliers
public class ItemDescLenMultipleRuleUnitTest {

    Receipt receipt;
    ReceiptPointRuleOptions options;
    ReceiptPointRuleCommand itemDescLenMultipleRule;


    @BeforeEach
    void setup() {
        receipt = spy(new Receipt("id", 0, "retailer", BigDecimal.ONE, LocalDateTime.now(),
                List.of(new ReceiptItem("shortDescription", BigDecimal.ONE))));
        options = new ReceiptPointRuleOptions();
    }

    @ParameterizedTest
    @MethodSource(value = "allGoodThreeMultipleValues")
    void awardsTrimmedLengthMultiples(List<ReceiptItem> items, BigDecimal multiplier, int multiple, int expectedPoints) {
        // setup
        receipt.setItems(items);
        receipt.setPoints(0);
        options = ReceiptPointRuleOptions.builder()
                .itemDescriptionLengthMultiple(multiple)
                .itemDescriptionPointMultiplier(multiplier)
                .build();
        itemDescLenMultipleRule = new ItemDescLenMultipleRule(receipt, options);
        int additionalPoints = expectedPoints;

        // when
        itemDescLenMultipleRule.applyRule();

        // then
        assertEquals(receipt.getPoints(), expectedPoints);
        verify(receipt, times(1)).addPoints(anyInt());
    }


    public static List<Arguments> allGoodThreeMultipleValues() {
        int multiple = 3;
        BigDecimal multiplier = new BigDecimal("0.20");
        List<TestValues> threeMultipleTestValues = List.of(
                new TestValues("123", "12.00", 3),
                new TestValues("abc", "6", 2),
                new TestValues("123abc", "3", 1),
                new TestValues("123456789", "12.00", 3),
                new TestValues("abcdefghi", "12.00", 3),
                new TestValues("          123", "12.00", 3),
                new TestValues("123      ", "12.00", 3),
                new TestValues("     123         ", "12.00", 3),
                new TestValues("123   abc", "12.00", 3),
                new TestValues("abc   123   ", "12.00", 3),
                new TestValues("     123   abc   ", "12.00", 3),
                new TestValues("1  123", "12.00", 3),
                new TestValues("  1  abc  ", "12.00", 3),
                new TestValues("   Klarbrunn 12-PK 12 FL OZ  ", "12.00", 3)
        );

        // Create multiple groups of items and their sums of expected values
        List<TestValuesGroup> testValuesGroups = createItemListGroupsFromTestValues(threeMultipleTestValues);
        return testValuesGroups.stream()
                .map(group -> Arguments.of(
                        group.testGroup.stream().map(value -> value.getItem()).toList(),
                        multiplier,
                        multiple,
                        group.totalExpectedValue)
                )
                .toList();
    }


    @ParameterizedTest
    @MethodSource(value = "someBadThreeMultipleValues")
    void awardsSomeTrimmedLengthMultiples(List<ReceiptItem> items, BigDecimal multiplier, int multiple, int expectedPoints) {
        // setup
        receipt.setItems(items);
        receipt.setPoints(0);
        options = ReceiptPointRuleOptions.builder()
                .itemDescriptionLengthMultiple(multiple)
                .itemDescriptionPointMultiplier(multiplier)
                .build();
        itemDescLenMultipleRule = new ItemDescLenMultipleRule(receipt, options);
        int additionalPoints = expectedPoints;

        // when
        itemDescLenMultipleRule.applyRule();

        // then
        assertEquals(receipt.getPoints(), expectedPoints);
        verify(receipt, times(1)).addPoints(anyInt());
    }

    public static List<Arguments> someBadThreeMultipleValues() {
        int multiple = 3;
        BigDecimal multiplier = new BigDecimal("0.20");
        List<TestValues> threeMultipleTestValues = List.of(
                new TestValues("1234", "12.00", 0),
                new TestValues("abc", "6", 2),
                new TestValues("123abcd", "3", 0),
                new TestValues("1234567890", "12.00", 0),
                new TestValues("abcdefghi", "12.00", 3),
                new TestValues("          123", "12.00", 3),
                new TestValues("123      ", "12.00", 3),
                new TestValues("     123         ", "12.00", 3),
                new TestValues("123   abc", "12.00", 3),
                new TestValues("abc   123   ", "12.00", 3),
                new TestValues("     123   abc   ", "12.00", 3),
                new TestValues("1  123", "12.00", 3),
                new TestValues("  1  abcd  ", "12.00", 0),
                new TestValues("   Klarbrunn 12-PK 12 FL O ", "12.00", 0)
        );

        // Create multiple groups of items and their sums of expected values
        List<TestValuesGroup> testValuesGroups = createItemListGroupsFromTestValues(threeMultipleTestValues);
        return testValuesGroups.stream()
                .map(group -> Arguments.of(
                        group.testGroup.stream().map(value -> value.getItem()).toList(),
                        multiplier,
                        multiple,
                        group.totalExpectedValue)
                )
                .toList();
    }


    @ParameterizedTest
    @MethodSource(value = "allBadThreeMultipleValues")
    void doesNotAwardBadTrimmedLengthMultiples(List<ReceiptItem> items, BigDecimal multiplier, int multiple, int expectedPoints) {
        // setup
        receipt.setItems(items);
        receipt.setPoints(0);
        options = ReceiptPointRuleOptions.builder()
                .itemDescriptionLengthMultiple(multiple)
                .itemDescriptionPointMultiplier(multiplier)
                .build();
        itemDescLenMultipleRule = new ItemDescLenMultipleRule(receipt, options);
        int additionalPoints = expectedPoints;

        // when
        itemDescLenMultipleRule.applyRule();

        // then
        assertEquals(receipt.getPoints(), expectedPoints);
        verify(receipt, times(1)).addPoints(anyInt());
    }

    public static List<Arguments> allBadThreeMultipleValues() {
        int multiple = 3;
        BigDecimal multiplier = new BigDecimal("0.20");
        List<TestValues> threeMultipleTestValues = List.of(
                new TestValues("1234", "12.00", 0),
                new TestValues("abcd", "6", 0),
                new TestValues("123abcd", "3", 0),
                new TestValues("1234567890", "12.00", 0),
                new TestValues("abcdefghij", "12.00", 0),
                new TestValues("          1234", "12.00", 0),
                new TestValues("1234      ", "12.00", 0),
                new TestValues("     1234         ", "12.00", 0),
                new TestValues("1234   abc", "12.00", 0),
                new TestValues("abc   1234   ", "12.00", 0),
                new TestValues("     1234   abc   ", "12.00", 0),
                new TestValues("1  1234", "12.00", 0),
                new TestValues("  1  abcd  ", "12.00", 0),
                new TestValues("   Klarbrunn 12-PK 12 FL O ", "12.00", 0)
        );

        // Create multiple groups of items and their sums of expected values
        List<TestValuesGroup> testValuesGroups = createItemListGroupsFromTestValues(threeMultipleTestValues);
        return testValuesGroups.stream()
                .map(group -> Arguments.of(
                        group.testGroup.stream().map(value -> value.getItem()).toList(),
                        multiplier,
                        multiple,
                        group.totalExpectedValue)
                )
                .toList();
    }

    /**
     * Used to combine multiple test values into separate lists with their expected total values
     * Each test value will also get put into its own list to run as a single unit test with
     * Receipts need a list of receipt items, and tests needs an expected total value of all of those items
     *
     * @return
     */
    public static List<TestValuesGroup> createItemListGroupsFromTestValues(List<TestValues> testValues) {
        // Create multiple groups of items and their sums of expected values
        List<TestValuesGroup> testGroups = new ArrayList<>();

        // Singular items
        testValues.stream().forEach(value -> {
            testGroups.add(new TestValuesGroup(List.of(value), value.expectedValue));
        });

        // All values together
        testGroups.add(
                new TestValuesGroup(testValues,
                        testValues.stream().mapToInt(value -> value.expectedValue).sum()
                )
        );

        // Arbitrary groups
        // TODO: remove hardcoded numbers ¯\_(ツ)_/¯
        List<TestValues> group1 = List.of(testValues.get(0), testValues.get(1), testValues.get(2));
        testGroups.add(new TestValuesGroup(group1, group1.stream().mapToInt(v -> v.expectedValue).sum()));

        List<TestValues> group2 = List.of(testValues.get(3), testValues.get(4), testValues.get(5));
        testGroups.add(new TestValuesGroup(group2, group2.stream().mapToInt(v -> v.expectedValue).sum()));

        List<TestValues> group3 = List.of(testValues.get(2), testValues.get(4), testValues.get(6));
        testGroups.add(new TestValuesGroup(group3, group3.stream().mapToInt(v -> v.expectedValue).sum()));

        return testGroups;
    }

    @Data
    public static class TestValues {
        ReceiptItem item;
        int expectedValue;

        public TestValues(String description, String price, int expectedValue) {
            this.expectedValue = expectedValue;
            this.item = new ReceiptItem(description, new BigDecimal(price));
        }
    }

    @Data
    @AllArgsConstructor
    public static class TestValuesGroup {
        List<TestValues> testGroup;
        int totalExpectedValue;
    }

}
