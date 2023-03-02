package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;

public class ItemGroupCountRule extends ReceiptPointRuleCommand {

    private final int itemGroupSize;
    private final int pointsPerGroup;

    public ItemGroupCountRule(Receipt receipt, ReceiptPointRuleOptions options) {
        super(receipt);
        itemGroupSize = options.getItemGroupSize();
        pointsPerGroup = options.getItemGroupPointCount();
    }

    @Override
    public void applyRule() {
        int itemGroups = receipt.getItems().size() / itemGroupSize;
        if (itemGroups > 0) {
            receipt.addPoints(itemGroups * pointsPerGroup);
        }
    }
}
