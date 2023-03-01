package com.fetch.jake.receiptprocessor.controller;

import com.fetch.jake.receiptprocessor.domain.GetReceiptPointsResponse;
import com.fetch.jake.receiptprocessor.domain.ProcessReceiptRequest;
import com.fetch.jake.receiptprocessor.domain.ProcessReceiptResponse;
import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/receipts")
public class ReceiptsApiController {

    @Autowired
    ReceiptService receiptService;

    @PostMapping("/process")
    public ResponseEntity<ProcessReceiptResponse> process(@RequestBody @Valid ProcessReceiptRequest processRequest) {
        log.info("Process request received: " + processRequest);
        String id = receiptService.processReceipt(processRequest);
        return new ResponseEntity<>(new ProcessReceiptResponse(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<GetReceiptPointsResponse> getReceiptPoints(@PathVariable @Valid String id) {
        log.info("Get Receipt Point request received for id: " + id);
        Receipt receipt = receiptService.getReceipt(id);
        return new ResponseEntity<>(new GetReceiptPointsResponse(receipt.getPoints()), HttpStatus.OK);
    }
}
