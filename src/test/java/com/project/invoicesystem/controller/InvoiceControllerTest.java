package com.project.invoicesystem.controller;
import com.project.invoicesystem.common.InvoiceStatus;
import com.project.invoicesystem.dto.InvoiceRequestDTO;
import com.project.invoicesystem.dto.InvoiceResponseDTO;
import com.project.invoicesystem.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInvoice_shouldReturnCreatedInvoice() {
        InvoiceRequestDTO requestDTO = new InvoiceRequestDTO();
        requestDTO.setAmount(100.0);
        requestDTO.setDueDate(LocalDate.of(2023, 12, 31));

        InvoiceResponseDTO responseDTO = new InvoiceResponseDTO(1L, 100.0, 0.0, LocalDate.of(2023, 12, 31), InvoiceStatus.PENDING);

        when(invoiceService.createInvoice(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<InvoiceResponseDTO> response = invoiceController.createInvoice(requestDTO);

        assertNotNull(response);
        assertNotNull(response.getBody(), "Response body is null");
        assertEquals(201, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
        assertEquals(InvoiceStatus.PENDING, response.getBody().getStatus());
    }

    @Test
    void getInvoices_shouldReturnAllInvoices() {
        InvoiceResponseDTO responseDTO = new InvoiceResponseDTO(1L, 100.0, 0.0, LocalDate.of(2023, 12, 31), InvoiceStatus.PENDING);

        when(invoiceService.getInvoices()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<InvoiceResponseDTO>> response = invoiceController.getInvoices();

        assertNotNull(response);
        assertNotNull(response.getBody(), "Response body is null");
        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody().isEmpty(), "Response body is empty");
    }

    @Test
    void payInvoice_shouldReturnUpdatedInvoice() {
        InvoiceResponseDTO responseDTO = new InvoiceResponseDTO(1L, 100.0, 100.0, LocalDate.of(2023, 12, 31), InvoiceStatus.PAID);

        when(invoiceService.payInvoice(1L, 100.0)).thenReturn(responseDTO);

        ResponseEntity<InvoiceResponseDTO> response = invoiceController.payInvoice(1L, Map.of("amount", 100.0));

        assertNotNull(response);
        assertNotNull(response.getBody(), "Response body is null");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(InvoiceStatus.PAID, response.getBody().getStatus());
        assertEquals(100.0, response.getBody().getPaidAmount());
    }
}
