package com.project.invoicesystem.service;

import com.project.invoicesystem.common.InvoiceStatus;
import com.project.invoicesystem.dto.InvoiceRequestDTO;
import com.project.invoicesystem.dto.InvoiceResponseDTO;
import com.project.invoicesystem.entity.Invoice;

import com.project.invoicesystem.mapper.InvoiceMapper;
import com.project.invoicesystem.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    private final InvoiceMapper invoiceMapper = InvoiceMapper.INSTANCE;

    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO) {
        Invoice invoice = invoiceMapper.toEntity(requestDTO);
        invoice.setStatus(InvoiceStatus.PENDING);
        System.out.println(invoice);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return invoiceMapper.toDto(savedInvoice);
    }

    public List<InvoiceResponseDTO> getInvoices() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::toDto)
                .collect(Collectors.toList());
    }

    public InvoiceResponseDTO payInvoice(Long id, double amount) {
        Invoice invoice = findInvoiceById(id);
        validatePendingInvoice(invoice);
        updatePaidAmount(invoice, amount);
        return invoiceMapper.toDto(invoiceRepository.save(invoice));
    }

    public void processOverdue(double lateFee, int overdueDays) {
        List<Invoice> overdueInvoices = findOverdueInvoices(overdueDays);
        overdueInvoices.forEach(invoice -> processSingleOverdueInvoice(invoice, lateFee));
    }

    // --- Refactored Methods ---

    private Invoice findInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
    }

    private void validatePendingInvoice(Invoice invoice) {
        if (!InvoiceStatus.PENDING.equals(invoice.getStatus())) {
            throw new IllegalArgumentException("Invalid or non-pending invoice");
        }
    }

    private void updatePaidAmount(Invoice invoice, double amount) {
        invoice.setPaidAmount(invoice.getPaidAmount() + amount);
        if (invoice.getPaidAmount() >= invoice.getAmount()) {
            invoice.setStatus(InvoiceStatus.PAID);
        }
    }

    private List<Invoice> findOverdueInvoices(int overdueDays) {
        LocalDate currentDate = LocalDate.now();
        return invoiceRepository.findAll().stream()
                .filter(invoice -> InvoiceStatus.PENDING.equals(invoice.getStatus())
                        && invoice.getDueDate().plusDays(overdueDays).isBefore(currentDate))
                .collect(Collectors.toList());
    }

    private void processSingleOverdueInvoice(Invoice invoice, double lateFee) {
        if (invoice.getPaidAmount() > 0) {
            markAsPaidAndCreateNewInvoice(invoice, lateFee);
        } else {
            markAsVoidAndCreateNewInvoice(invoice, lateFee);
        }
        invoiceRepository.save(invoice);
    }

    private void markAsPaidAndCreateNewInvoice(Invoice invoice, double lateFee) {
        invoice.setStatus(InvoiceStatus.PAID);
        double remainingAmount = invoice.getAmount() - invoice.getPaidAmount() + lateFee;
        Invoice newInvoice = new Invoice(remainingAmount, LocalDate.now().plusDays(30));
        invoiceRepository.save(newInvoice);
    }

    private void markAsVoidAndCreateNewInvoice(Invoice invoice, double lateFee) {
        invoice.setStatus(InvoiceStatus.VOID);
        double newAmount = invoice.getAmount() + lateFee;
        Invoice newInvoice = new Invoice(newAmount, LocalDate.now().plusDays(30));
        invoiceRepository.save(newInvoice);
    }
}
