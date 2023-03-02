package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;

import java.math.BigDecimal;

public class TotalRoundDollarRule extends ReceiptPointRuleCommand {
    private final int pointsToAdd;

    public TotalRoundDollarRule(Receipt receipt, ReceiptPointRuleOptions options) {
        super(receipt);
        pointsToAdd = options.getEvenTotalPoints();
    }

    @Override
    public void applyRule() {
        // Use remainder / modulus operator to get only cent amount
        BigDecimal totalCents = receipt.getTotal().remainder(BigDecimal.ONE);
        if (totalCents.signum() == 0) {
            receipt.addPoints(pointsToAdd);
        }
    }
}
