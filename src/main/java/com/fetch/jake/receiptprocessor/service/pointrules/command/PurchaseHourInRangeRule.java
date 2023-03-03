package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;

import java.time.LocalTime;

public class PurchaseHourInRangeRule extends ReceiptPointRuleCommand {

    private final int pointsToAdd;
    private final LocalTime rangeStart;
    private final LocalTime rangeEnd;

    public PurchaseHourInRangeRule(Receipt receipt, ReceiptPointRuleOptions options) {
        super(receipt);
        pointsToAdd = options.getPurchaseTimeInRangePoints();
        rangeStart = options.getPurchaseTimeRangeStart();
        rangeEnd = options.getPurchaseTimeRangeEnd();
    }

    @Override
    public void applyRule() {
        LocalTime receiptPurchaseTime = receipt.getPurchaseDateTime().toLocalTime();

        // Check if purchase time is inside the specified time range
        if (rangeStart.compareTo(receiptPurchaseTime) <= 0) {
            if (rangeEnd.compareTo(receiptPurchaseTime) >= 0) {
                receipt.addPoints(pointsToAdd);
            }
        }
    }
}
