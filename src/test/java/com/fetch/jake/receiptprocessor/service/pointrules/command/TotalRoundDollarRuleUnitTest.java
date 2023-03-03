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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class TotalRoundDollarRuleUnitTest {

    Receipt receipt;
    ReceiptPointRuleOptions options;
    ReceiptPointRuleCommand roundDollarCommand;


    @BeforeEach
    void setup() {
        receipt = spy(new Receipt("id", 0, "retailer", BigDecimal.ONE, LocalDateTime.now(),
                List.of(new ReceiptItem("shortDescription", BigDecimal.ONE))));
        options = new ReceiptPointRuleOptions();
        roundDollarCommand = new TotalRoundDollarRule(receipt, options);
    }

    @ParameterizedTest
    @MethodSource(value = "roundTotalInputs")
    void roundTotals(BigDecimal total, int startingPoints) {
        // setup
        receipt.setTotal(total);
        receipt.setPoints(startingPoints);
        int additionalPoints = options.getEvenTotalPoints();

        // when
        roundDollarCommand.applyRule();

        // then
        assertEquals(receipt.getPoints(), startingPoints + additionalPoints);
        verify(receipt, times(1)).addPoints(additionalPoints);
    }

    public static List<Arguments> roundTotalInputs() {
        return List.of(
                Arguments.of(BigDecimal.valueOf(1.00), 0),
                Arguments.of(BigDecimal.valueOf(2.00), 1),
                Arguments.of(BigDecimal.valueOf(3.00), 2),
                Arguments.of(BigDecimal.valueOf(5.00), 5),
                Arguments.of(BigDecimal.valueOf(40.00), 15),
                Arguments.of(BigDecimal.valueOf(125.00), 22),
                Arguments.of(BigDecimal.valueOf(500040.00), 10052)
        );
    }


    @ParameterizedTest
    @MethodSource(value = "notRoundTotalInputs")
    void notRoundTotals(BigDecimal total, int startingPoints) {
        receipt.setTotal(total);
        receipt.setPoints(startingPoints);
        int additionalPoints = options.getEvenTotalPoints();

        roundDollarCommand.applyRule();

        assertEquals(receipt.getPoints(), startingPoints);
        verify(receipt, times(0)).addPoints(additionalPoints);
    }

    public static List<Arguments> notRoundTotalInputs() {
        return List.of(
                Arguments.of(BigDecimal.valueOf(1.01), 0),
                Arguments.of(BigDecimal.valueOf(2.10), 1),
                Arguments.of(BigDecimal.valueOf(3.20), 2),
                Arguments.of(BigDecimal.valueOf(5.50), 5),
                Arguments.of(BigDecimal.valueOf(40.75), 15),
                Arguments.of(BigDecimal.valueOf(125.99), 22),
                Arguments.of(BigDecimal.valueOf(500040.102), 10052)
        );
    }
}
