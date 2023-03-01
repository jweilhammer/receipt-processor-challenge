package com.fetch.jake.receiptprocessor.service;

import com.fetch.jake.receiptprocessor.domain.ProcessReceiptRequest;
import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.model.ReceiptItem;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
public class ReceiptService {

    @Autowired
    PointRules receiptPointCommandMap;

    public static final Map<String, Consumer<Receipt>> receiptPointsRuleMap = PointRules.receiptPointsRules;

    public String processReceipt(ProcessReceiptRequest processRequest) {
        // Model receipt and items from request DTO
        Receipt requestReceipt = convertReceiptRequestToEntity(processRequest);
        applyPoints(requestReceipt);
        return requestReceipt.getId();
    }


    public void applyPoints(Receipt receipt) {
        receiptPointsRuleMap.values().forEach(
            rule -> rule.accept(receipt)
        );
        log.info("Points have been applied to receipt: " + receipt);
    }

    /**
     * Provides an abstraction between what users send as requests, and what we map
     * as data entities. This allows re-modeling the request in ways that make sense
     * for us, and future API versions
     *
     * For example: request includes date + time separately, but we can convert into
     * a full datetime object here
     *
     * @param processRequest data object with validated values mapped from user request
     * @return               The receipt that models the information from the request
     */
    public Receipt convertReceiptRequestToEntity(ProcessReceiptRequest processRequest) {
        List<ReceiptItem> requestItems = new ArrayList<>();
        processRequest.getItems().forEach((requestItem) -> {
            // Do any extra modeling logic here
            requestItems.add(new ReceiptItem(requestItem.getShortDescription(), requestItem.getPrice()));
        });

        Receipt requestReceipt = Receipt.builder()
                .id(UUID.randomUUID().toString())
                .points(0)
                .retailer(processRequest.getRetailer())
                .purchaseDateTime(LocalDateTime.of(processRequest.getPurchaseDate(), processRequest.getPurchaseTime()))
                .items(requestItems)
                .total(processRequest.getTotal())
                .build();

        return requestReceipt;
    }
}
