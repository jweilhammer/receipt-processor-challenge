package com.fetch.jake.receiptprocessor.service;

import com.fetch.jake.receiptprocessor.domain.ProcessReceiptRequest;
import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.model.ReceiptItem;
import com.fetch.jake.receiptprocessor.repository.ReceiptRepository;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleCommandFactory;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReceiptService {

    @Autowired
    ReceiptRepository repository;

    @Autowired
    ReceiptPointRuleCommandFactory pointRuleFactory;

    @Autowired
    ReceiptPointRuleOptions defaultReceiptPointRuleOptions;


    public String processReceipt(ProcessReceiptRequest processRequest) {
        // Model receipt and items from request DTO
        Receipt requestReceipt = convertReceiptRequestToEntity(processRequest);
        applyPoints(requestReceipt, defaultReceiptPointRuleOptions);
        repository.saveReceipt(requestReceipt);
        return requestReceipt.getId();
    }

    public Receipt getReceipt(String id) {
        return repository.getReceipt(id);
    }

    public void applyPoints(Receipt receipt, ReceiptPointRuleOptions options) {
        pointRuleFactory.getAllCommands(receipt, options).forEach(
                rule -> rule.applyRule()
        );

        log.info("Points have been applied to receipt: " + receipt);
    }

    /**
     * Provides an abstraction between what users send as requests, and what we map
     * as data entities. This allows re-modeling the request in ways that make sense
     * for us, and future API versions
     * <p>
     * For example: request includes date + time separately, but we can convert into
     * a full datetime object here
     *
     * @param processRequest data object with validated values mapped from user request
     * @return The receipt that models the information from the request
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
                .purchaseDateTime(LocalDateTime.of(
                        processRequest.getPurchaseDate(), processRequest.getPurchaseTime().truncatedTo(ChronoUnit.MINUTES))
                )
                .items(requestItems)
                .total(processRequest.getTotal())
                .build();

        return requestReceipt;
    }
}
