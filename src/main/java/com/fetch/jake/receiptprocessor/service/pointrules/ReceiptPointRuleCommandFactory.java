package com.fetch.jake.receiptprocessor.service.pointrules;


import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.service.pointrules.command.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReceiptPointRuleCommandFactory {

    static ReceiptPointRuleCommandType[] commandTypes = ReceiptPointRuleCommandType.values();
    static int totalCommandCount = ReceiptPointRuleCommandType.values().length;

    public ReceiptPointRuleCommand getCommand(ReceiptPointRuleCommandType ruleType, Receipt receipt, ReceiptPointRuleOptions options) {
        switch (ruleType) {
            case ITEM_DESC_LENGTH_MULTIPLE:
                return new ItemDescLenMultipleRule(receipt, options);
            case ITEM_GROUP_COUNT:
                return new ItemGroupCountRule(receipt, options);
            case PURCHASE_DAY_ODD:
                return new PurchaseDayOddRule(receipt, options);
            case PURCHASE_HOUR_IN_RANGE:
                return new PurchaseHourInRangeRule(receipt, options);
            case TOTAL_QUARTER_MULTIPLE:
                return new TotalQuarterMultipleRule(receipt, options);
            case RETAILER_NAME_CHAR_COUNT:
                return new RetailerNameCharCountRule(receipt, options);
            case TOTAL_ROUND_DOLLAR:
                return new TotalRoundDollarRule(receipt, options);
            default:
                throw new IllegalArgumentException("Rule type: " + ruleType + " has not been implemented");
        }
    }

    public List<ReceiptPointRuleCommand> getAllCommands(Receipt receipt, ReceiptPointRuleOptions options) {
        List<ReceiptPointRuleCommand> ruleCommands = new ArrayList<>(totalCommandCount);
        for (ReceiptPointRuleCommandType commandType : commandTypes) {
            ruleCommands.add(getCommand(commandType, receipt, options));
        }

        return ruleCommands;
    }

}
