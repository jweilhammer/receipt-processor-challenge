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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class PurchaseHourInRangeRuleUnitTest {

    Receipt receipt;
    ReceiptPointRuleOptions options;
    ReceiptPointRuleCommand purchaseHourBetweenCommand;


    @BeforeEach
    void setup() {
        receipt = spy(new Receipt("id", 0, "retailer", BigDecimal.ONE, LocalDateTime.now(),
                List.of(new ReceiptItem("shortDescription", BigDecimal.ONE))));
    }

    @ParameterizedTest
    @MethodSource(value = "insideThresholdInputs")
    void awardsHoursInBetweenThreshold(LocalTime purchaseTime, LocalTime thresholdStart, LocalTime thresholdEnd) {
        // setup
        receipt.setPurchaseDateTime(LocalDateTime.of(LocalDate.now(), purchaseTime));
        receipt.setPoints(0);
        options = ReceiptPointRuleOptions.builder()
                .purchaseTimeRangeStart(thresholdStart)
                .purchaseTimeRangeEnd(thresholdEnd)
                .build();
        purchaseHourBetweenCommand = new PurchaseHourInRangeRule(receipt, options);

        int additionalPoints = options.getPurchaseTimeInRangePoints();

        // when
        purchaseHourBetweenCommand.applyRule();

        // then
        assertEquals(receipt.getPoints(), 0);
        verify(receipt, times(1)).addPoints(additionalPoints);
    }

    public static Stream<Arguments> insideThresholdInputs() {
        return Stream.of(
                // start == time, time < end
                Arguments.of(LocalTime.of(0, 0), LocalTime.of(0, 0), LocalTime.of(0, 1)),
                Arguments.of(LocalTime.of(0, 0), LocalTime.of(0, 0), LocalTime.of(0, 59)),
                Arguments.of(LocalTime.of(0, 0), LocalTime.of(0, 0), LocalTime.of(1, 0)),
                Arguments.of(LocalTime.of(2, 0), LocalTime.of(2, 0), LocalTime.of(12, 0)),
                Arguments.of(LocalTime.of(12, 0), LocalTime.of(12, 0), LocalTime.of(23, 0)),

                // start < time, time == end
                Arguments.of(LocalTime.of(0, 1), LocalTime.of(0, 0), LocalTime.of(0, 1)),
                Arguments.of(LocalTime.of(0, 59), LocalTime.of(0, 0), LocalTime.of(0, 59)),
                Arguments.of(LocalTime.of(1, 0), LocalTime.of(0, 0), LocalTime.of(1, 0)),
                Arguments.of(LocalTime.of(12, 0), LocalTime.of(2, 0), LocalTime.of(12, 0)),
                Arguments.of(LocalTime.of(23, 45), LocalTime.of(12, 0), LocalTime.of(23, 45)),

                // start < time, time < end
                Arguments.of(LocalTime.of(0, 2), LocalTime.of(0, 0), LocalTime.of(0, 5)),
                Arguments.of(LocalTime.of(0, 25), LocalTime.of(0, 10), LocalTime.of(0, 59)),
                Arguments.of(LocalTime.of(2, 0), LocalTime.of(1, 0), LocalTime.of(5, 0)),
                Arguments.of(LocalTime.of(14, 1), LocalTime.of(14, 0), LocalTime.of(16, 0)),
                Arguments.of(LocalTime.of(12, 20), LocalTime.of(12, 10), LocalTime.of(12, 30)),


                // start == time, time == end
                Arguments.of(LocalTime.of(0, 0), LocalTime.of(0, 0), LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(23, 59), LocalTime.of(23, 59), LocalTime.of(23, 59))
        );
    }

    @ParameterizedTest
    @MethodSource(value = "outsideThresholdInputs")
    void doesNotAwardHoursOutsideThreshold(LocalTime purchaseTime, LocalTime thresholdStart, LocalTime thresholdEnd) {
        // setup
        receipt.setPurchaseDateTime(LocalDateTime.of(LocalDate.now(), purchaseTime));
        receipt.setPoints(0);
        options = ReceiptPointRuleOptions.builder()
                .purchaseTimeRangeStart(thresholdStart)
                .purchaseTimeRangeEnd(thresholdEnd)
                .build();
        purchaseHourBetweenCommand = new PurchaseHourInRangeRule(receipt, options);
        int additionalPoints = options.getPurchaseTimeInRangePoints();

        // when
        purchaseHourBetweenCommand.applyRule();

        // then
        assertEquals(receipt.getPoints(), 0);
        verify(receipt, times(0)).addPoints(additionalPoints);
    }

    public static Stream<Arguments> outsideThresholdInputs() {
        return Stream.of(
                // time < start
                Arguments.of(LocalTime.of(0, 0), LocalTime.of(0, 1), LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(0, 30), LocalTime.of(0, 59), LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(0, 0), LocalTime.of(1, 0), LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(3, 0), LocalTime.of(12, 0), LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(23, 58), LocalTime.of(23, 59), LocalTime.of(0, 0)),

                // start == time, time > end
                Arguments.of(LocalTime.of(0, 59), LocalTime.of(0, 59), LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(1, 0), LocalTime.of(1, 0), LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(2, 30), LocalTime.of(2, 30), LocalTime.of(1, 0)),
                Arguments.of(LocalTime.of(23, 0), LocalTime.of(23, 0), LocalTime.of(12, 0)),

                // start < time, time > end
                Arguments.of(LocalTime.of(0, 2), LocalTime.of(0, 0), LocalTime.of(0, 1)),
                Arguments.of(LocalTime.of(0, 59), LocalTime.of(0, 0), LocalTime.of(0, 58)),
                Arguments.of(LocalTime.of(2, 0), LocalTime.of(0, 0), LocalTime.of(1, 0)),
                Arguments.of(LocalTime.of(13, 0), LocalTime.of(2, 0), LocalTime.of(12, 0)),
                Arguments.of(LocalTime.of(23, 59), LocalTime.of(12, 0), LocalTime.of(23, 45))
        );
    }
}
