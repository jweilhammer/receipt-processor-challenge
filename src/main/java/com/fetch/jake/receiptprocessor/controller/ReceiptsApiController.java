package com.fetch.jake.receiptprocessor.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/receipts")
public class ReceiptsApiController {

    @PostMapping("/process")
    public ResponseEntity<String> process() {
        return new ResponseEntity<>("process", HttpStatus.OK);
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<String> getReceiptPoints() {
        return new ResponseEntity<>("getReceiptPoints", HttpStatus.OK);
    }
}
