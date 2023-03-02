package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;

public class PurchaseDayOddRule extends ReceiptPointRuleCommand {

    private final int pointsToAdd;

    public PurchaseDayOddRule(Receipt receipt, ReceiptPointRuleOptions options) {
        super(receipt);
        pointsToAdd = options.getOddPurchaseDayPoints();
    }

    @Override
    public void applyRule() {
        if (receipt.getPurchaseDateTime().getDayOfMonth() % 2 == 1) {
            receipt.addPoints(pointsToAdd);
        }
    }
}
