package com.fetch.jake.receiptprocessor.controller;

import com.fetch.jake.receiptprocessor.domain.GetReceiptPointsResponse;
import com.fetch.jake.receiptprocessor.domain.ProcessReceiptRequest;
import com.fetch.jake.receiptprocessor.domain.ProcessReceiptResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/receipts")
public class ReceiptsApiController {

    @PostMapping("/process")
    public ResponseEntity<ProcessReceiptResponse> process(@RequestBody @Valid ProcessReceiptRequest processRequest) {
        log.info("Process request received: " + processRequest);
        return new ResponseEntity<>(new ProcessReceiptResponse("id-12345"), HttpStatus.OK);
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<GetReceiptPointsResponse> getReceiptPoints(@PathVariable @Valid String id) {
        log.info("Get Receipt Point request received for id: " + id);
        return new ResponseEntity<>(new GetReceiptPointsResponse(12345), HttpStatus.OK);
    }
}
