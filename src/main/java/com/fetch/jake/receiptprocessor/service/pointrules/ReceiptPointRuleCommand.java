package com.fetch.jake.receiptprocessor.service.pointrules;

import com.fetch.jake.receiptprocessor.model.Receipt;

public abstract class ReceiptPointRuleCommand {

    protected final Receipt receipt;

    public ReceiptPointRuleCommand(Receipt receipt) {
        this.receipt = receipt;
    }

    public abstract void applyRule();
}