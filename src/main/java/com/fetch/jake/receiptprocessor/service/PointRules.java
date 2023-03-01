package com.fetch.jake.receiptprocessor.service;//package com.fetch.jake.receipts.service;

import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.model.ReceiptItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// Purely functional class to hold all filters for receipt point calculations
@Slf4j
@Component
public class PointRules {

	// Regex pattern to match any non-alphanumerical chars
	public static final String NON_ALPHA_NUMERIC_REGEX = "[^a-zA-Z\\d\\s:]+";

	// Constants for points logic
	static final BigDecimal ITEM_DESCRIPTION_POINT_MULTIPLIER = BigDecimal.valueOf(0.20).setScale(2);
	static final int ITEM_DESCRIPTION_LENGTH_MULTIPLE = 3;
	static final int PURCHASE_HOUR_START = 12;
	static final int PURCHASE_HOUR_END = 14;

	// Point addition amounts
	public static final int RETAILER_CHAR_COUNT_POINT_MULTIPLIER = 1;
	static final int QUARTER_MULITPLE_TOTAL_POINTS = 25;
	static final int ODD_PURCHASE_DAY_POINTS = 6;
	static final int ITEM_GROUP_POINT_COUNT = 5;
	static final int PURCHASE_HOUR_POINTS = 10;
	static final int EVEN_TOTAL_POINTS = 50;

	public static final Map<String, Consumer<Receipt>> receiptPointsRules = new HashMap<>() {
		{
			put("dongo", (r) -> retailerName(r));
			put("evenDollar", (r) -> evenDollar(r));
			put("quarterMultiple", (r) -> quarterMultiple(r));
			put("itemCountRules", (r) -> itemCount(r));
			put("itemDescription", (r) -> itemDescription(r));
			put("oddPurchaseDay", (r) -> oddPurchaseDay(r));
			put("purchaseHour", (r) -> purchaseHour(r));
		}
	};

	// One point for every alphanumeric character in the retailer name
	private static void retailerName(Receipt receipt) {
		String alphaNumericRetailerName = receipt.getRetailer().replaceAll(NON_ALPHA_NUMERIC_REGEX, "");
		log.info("CLEANED NAME: " + alphaNumericRetailerName);
		int additionalPoints = alphaNumericRetailerName.length() * RETAILER_CHAR_COUNT_POINT_MULTIPLIER;
		receipt.setPoints(receipt.getPoints() + additionalPoints);
	}

	// 50 points if the total is a round dollar amount with no cents.
	static void evenDollar(Receipt receipt) {

		// Use remainder / modulus operator to get only cent amount
		BigDecimal totalCents = receipt.getTotal().remainder(BigDecimal.ONE);
		if (totalCents.signum() == 0) {
			log.info("TOTAL IS AN EVEN DOLLAR AMOUNT: " + totalCents);
			receipt.setPoints(receipt.getPoints() + EVEN_TOTAL_POINTS);
		}
	}

	// 25 points if the total is a multiple of 0.25.
	// Use remainder / modulus operator with 0.25 to get if it's a multiple
	static void quarterMultiple(Receipt receipt) {
		BigDecimal totalCents = receipt.getTotal().remainder(BigDecimal.ONE);
		if (totalCents.remainder(BigDecimal.valueOf(0.25)).signum() == 0) {
			log.info("TOTAL IS A MULTIPLE OF 0.25: " + totalCents + ", "
					+ totalCents.remainder(BigDecimal.valueOf(0.25)));
			receipt.setPoints(receipt.getPoints() + QUARTER_MULITPLE_TOTAL_POINTS);
		}
	}

	// 5 points for every two items on the receipt.
	static void itemCount(Receipt receipt) {
		int itemCount = receipt.getItems().size();
		log.info("ITEMS TOTAL: " + itemCount);
		log.info("ITEM PAIRS: " + itemCount / 2);
		receipt.setPoints(receipt.getPoints() + (itemCount / 2 * ITEM_GROUP_POINT_COUNT));
	}

	// If the trimmed length of the item description is a multiple of 3,
	// multiply the price by 0.2 and round up to the nearest integer
	// The result is the number of points earned.
	static void itemDescription(Receipt receipt) {
		int additionalPoints = 0;
		for (ReceiptItem item : receipt.getItems()) {
			log.info(item + ", " + item.getShortDescription().trim().length() + ", "
					+ item.getShortDescription().trim().length() % ITEM_DESCRIPTION_LENGTH_MULTIPLE);
			if (item.getShortDescription().trim().length() % ITEM_DESCRIPTION_LENGTH_MULTIPLE == 0) {
				log.info(item + " IS THE RIGHT LENGTH!");
				log.info("MULTIPLYING " + item.getPrice() + " x " + ITEM_DESCRIPTION_POINT_MULTIPLIER);
				log.info("ADDED POINTS ARE: " + item.getPrice().multiply(ITEM_DESCRIPTION_POINT_MULTIPLIER)
						.setScale(0, RoundingMode.UP).intValue());
				additionalPoints += item.getPrice()
						.multiply(ITEM_DESCRIPTION_POINT_MULTIPLIER)
						.setScale(0, RoundingMode.UP)
						.intValue();
			}
		}

		receipt.setPoints(receipt.getPoints() + additionalPoints);
	}

	// 6 points if the day in the purchase date is odd.
	static void oddPurchaseDay(Receipt receipt) {
		if (receipt.getPurchaseDateTime().getDayOfMonth() % 2 == 1) {
			log.info("DAY OF MONTH IS ODD: " + receipt.getPurchaseDateTime().getDayOfMonth());
			receipt.setPoints(receipt.getPoints() + ODD_PURCHASE_DAY_POINTS);
		}
	}

	// 10 points if the time of purchase is after 2:00pm and before 4:00pm.
	static void purchaseHour(Receipt receipt) {
		int purchaseHour = receipt.getPurchaseDateTime().getHour();
		if (purchaseHour >= PURCHASE_HOUR_START && purchaseHour < PURCHASE_HOUR_END) {
			log.info("TIME IS IN SWEET SPOT: " + purchaseHour);
			receipt.setPoints(receipt.getPoints() + PURCHASE_HOUR_POINTS);
		}
	}
}
