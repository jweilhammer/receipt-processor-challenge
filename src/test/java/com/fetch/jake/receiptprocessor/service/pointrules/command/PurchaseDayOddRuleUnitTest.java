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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class PurchaseDayOddRuleUnitTest {

    Receipt receipt;
    ReceiptPointRuleOptions options;
    ReceiptPointRuleCommand purchaseDayOddCommand;


    @BeforeEach
    void setup() {
        receipt = spy(new Receipt("id", 0, "retailer", BigDecimal.ONE, LocalDateTime.now(),
                List.of(new ReceiptItem("shortDescription", BigDecimal.ONE))));
        options = new ReceiptPointRuleOptions();
        purchaseDayOddCommand = new PurchaseDayOddRule(receipt, options);
    }


    @ParameterizedTest
    @MethodSource(value = "oddDayInputs")
    void awardsPointsForOddDays(int month, int day) {
        // setup
        receipt.setPurchaseDateTime(LocalDateTime.now().withDayOfMonth(day));
        receipt.setPoints(0);
        int additionalPoints = options.getOddPurchaseDayPoints();

        // when
        purchaseDayOddCommand.applyRule();

        // then
        assertEquals(receipt.getPoints(), additionalPoints);
        verify(receipt, times(1)).addPoints(additionalPoints);
    }

    public static List<Arguments> oddDayInputs() {
        return List.of(
                // Month, Day
                Arguments.of(1, 1),
                Arguments.of(2, 3),
                Arguments.of(3, 5),
                Arguments.of(4, 7),
                Arguments.of(5, 9),
                Arguments.of(6, 11),
                Arguments.of(7, 13),
                Arguments.of(8, 15),
                Arguments.of(9, 17),
                Arguments.of(10, 19),
                Arguments.of(11, 21),
                Arguments.of(12, 23),
                Arguments.of(1, 25),
                Arguments.of(2, 27),
                Arguments.of(3, 29),
                Arguments.of(4, 31)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "evenDayInputs")
    void doesNotAwardPointsForEvenDays(int month, int day) {
        // setup
        receipt.setPurchaseDateTime(LocalDateTime.of(LocalDate.now().withDayOfMonth(month).withDayOfYear(day), LocalTime.now()));
        receipt.setPoints(0);
        int additionalPoints = options.getOddPurchaseDayPoints();

        // when
        purchaseDayOddCommand.applyRule();

        // then
        assertEquals(receipt.getPoints(), 0);
        verify(receipt, times(0)).addPoints(additionalPoints);
    }

    public static List<Arguments> evenDayInputs() {
        return List.of(
                // Month, Day
                Arguments.of(1, 2),
                Arguments.of(2, 4),
                Arguments.of(3, 6),
                Arguments.of(4, 8),
                Arguments.of(5, 10),
                Arguments.of(6, 12),
                Arguments.of(7, 14),
                Arguments.of(8, 16),
                Arguments.of(9, 18),
                Arguments.of(10, 20),
                Arguments.of(11, 22),
                Arguments.of(12, 24),
                Arguments.of(1, 26),
                Arguments.of(2, 28),
                Arguments.of(3, 30)
        );
    }
}
