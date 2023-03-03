package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.model.ReceiptItem;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;
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


public class ItemGroupCountRuleUnitTest {

    Receipt receipt;
    ReceiptPointRuleOptions options;
    ReceiptPointRuleCommand itemGroupCountCommand;


    @BeforeEach
    void setup() {
        receipt = spy(new Receipt("id", 0, "retailer", BigDecimal.ZERO, LocalDateTime.now(),

                List.of(new ReceiptItem("shortDescription", BigDecimal.ONE))));
    }

    @ParameterizedTest
    @MethodSource(value = "goodItemGroups")
    void awardsPointsForItemGroups(List<ReceiptItem> itemList, int groupMultiple, int pointsPerGroup, int expectedPoints) {
        // setup
        receipt.setItems(itemList);
        receipt.setPoints(0);
        options = ReceiptPointRuleOptions.builder()
                .itemGroupSize(groupMultiple)
                .itemGroupPointCount(pointsPerGroup)
                .build();
        itemGroupCountCommand = new ItemGroupCountRule(receipt, options);
        int additionalPoints = expectedPoints;

        // when
        itemGroupCountCommand.applyRule();

        // then
        assertEquals(receipt.getPoints(), expectedPoints);
        verify(receipt, times(1)).addPoints(anyInt());
    }


    public static List<Arguments> goodItemGroups() {

        List<TestValues> tests = List.of(
                // ItemListSize, GroupMultiple, PointsPerGroup, ExpectedPoints

                // One group
                new TestValues(1, 1, 5, 5),
                new TestValues(2, 2, 5, 5),
                new TestValues(5, 5, 5, 5),
                new TestValues(27, 27, 5, 5),

                // Two+ groups
                new TestValues(4, 2, 5, 10),
                new TestValues(6, 2, 5, 15),
                new TestValues(8, 2, 5, 20),
                new TestValues(10, 2, 5, 25),
                new TestValues(6, 3, 5, 10),
                new TestValues(9, 3, 5, 15),
                new TestValues(12, 3, 5, 20),
                new TestValues(15, 3, 5, 25),

                // Higher values
                new TestValues(4, 2, 17, 34),
                new TestValues(6, 2, 20, 60),
                new TestValues(8, 2, 111, 444),
                new TestValues(1, 1, 12, 12),
                new TestValues(2, 2, 37, 37),
                new TestValues(5, 5, 52, 52)
        );

        return tests.stream().map(
                test -> Arguments.of(test.itemList, test.groupMultiple, test.pointsPerGroup, test.expectedPoints)
        ).toList();
    }


    @ParameterizedTest
    @MethodSource(value = "noItemGroups")
    void doesNotAwardPointsForNoItemGroups(List<ReceiptItem> itemList, int groupMultiple, int pointsPerGroup, int expectedPoints) {
        // setup
        receipt.setItems(itemList);
        receipt.setPoints(0);
        options = ReceiptPointRuleOptions.builder()
                .itemGroupSize(groupMultiple)
                .itemGroupPointCount(pointsPerGroup)
                .build();
        itemGroupCountCommand = new ItemGroupCountRule(receipt, options);

        // when
        itemGroupCountCommand.applyRule();

        // then
        assertEquals(receipt.getPoints(), 0);
        verify(receipt, times(0)).addPoints(anyInt());
    }


    public static List<Arguments> noItemGroups() {

        List<TestValues> tests = List.of(
                // ItemListSize, GroupMultiple, PointsPerGroup, ExpectedPoints

                // No groups
                new TestValues(0, 1, 5, 0),
                new TestValues(2, 3, 5, 0),
                new TestValues(3, 4, 5, 0),
                new TestValues(4, 5, 5, 0),
                new TestValues(37, 38, 5, 0),
                new TestValues(38, 39, 5, 0)
        );

        return tests.stream().map(
                test -> Arguments.of(test.itemList, test.groupMultiple, test.pointsPerGroup, test.expectedPoints)
        ).toList();
    }

    @Data
    public static class TestValues {
        int itemListSize;
        int groupMultiple;
        int pointsPerGroup;
        int expectedPoints;


        List<ReceiptItem> itemList;

        public TestValues(int itemListSize, int groupMultiple, int pointsPerGroup, int expectedPoints) {
            this.itemListSize = itemListSize;
            this.groupMultiple = groupMultiple;
            this.pointsPerGroup = pointsPerGroup;
            this.expectedPoints = expectedPoints;

            // Generate list of receipt items with specified size
            this.itemList = new ArrayList<>();
            for (int i = 0; i < itemListSize; i++) {
                this.itemList.add(new ReceiptItem("description", new BigDecimal("1.23")));
            }
        }
    }
}
