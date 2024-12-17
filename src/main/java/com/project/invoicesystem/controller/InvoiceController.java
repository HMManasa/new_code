package com.project.invoicesystem.controller;

import com.project.invoicesystem.dto.InvoiceRequestDTO;
import com.project.invoicesystem.dto.InvoiceResponseDTO;
import com.project.invoicesystem.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO invoiceRequest) {
        System.out.println("Received Due Date: " + LocalDate.now());
        InvoiceResponseDTO response = invoiceService.createInvoice(invoiceRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoices() {
        List<InvoiceResponseDTO> response = invoiceService.getInvoices();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<InvoiceResponseDTO> payInvoice(@PathVariable Long id, @RequestBody Map<String, Double> paymentRequest) {
        double amount = paymentRequest.get("amount");
        InvoiceResponseDTO response = invoiceService.payInvoice(id, amount);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/process-overdue")
    public ResponseEntity<Void> processOverdue(@RequestBody Map<String, Object> request) {
        double lateFee = (double) request.get("late_fee");
        int overdueDays = (int) request.get("overdue_days");
        invoiceService.processOverdue(lateFee, overdueDays);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
