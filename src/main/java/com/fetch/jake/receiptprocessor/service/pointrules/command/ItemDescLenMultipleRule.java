package com.fetch.jake.receiptprocessor.service.pointrules.command;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.model.ReceiptItem;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommand;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ItemDescLenMultipleRule extends ReceiptPointRuleCommand {

    private final int lengthMultiple;
    private final BigDecimal priceMultiplier;

    public ItemDescLenMultipleRule(Receipt receipt, ReceiptPointRuleOptions options) {
        super(receipt);
        lengthMultiple = options.getItemDescriptionLengthMultiple();
        priceMultiplier = options.getItemDescriptionPointMultiplier();
    }

    @Override
    public void applyRule() {
        int additionalPoints = 0;
        for (ReceiptItem item : receipt.getItems()) {
            if (item.getShortDescription().trim().length() % lengthMultiple == 0) {
                // Multiply price and round up to the nearest int
                additionalPoints += item.getPrice()
                        .multiply(priceMultiplier)
                        .setScale(0, RoundingMode.UP)
                        .intValue();
            }
        }

        receipt.addPoints(additionalPoints);
    }
}
