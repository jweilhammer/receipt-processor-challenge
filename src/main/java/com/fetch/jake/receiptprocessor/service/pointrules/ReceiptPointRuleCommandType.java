package com.fetch.jake.receiptprocessor.service.pointrules;

public enum ReceiptPointRuleCommandType {
    RETAILER_NAME_CHAR_COUNT,
    ITEM_DESC_LENGTH_MULTIPLE,
    ITEM_GROUP_COUNT,
    PURCHASE_DAY_ODD,
    PURCHASE_HOUR_IN_RANGE,
    TOTAL_QUARTER_MULTIPLE,
    TOTAL_ROUND_DOLLAR
}