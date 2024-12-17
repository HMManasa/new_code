package com.project.invoicesystem.service;
import com.project.invoicesystem.common.InvoiceStatus;
import com.project.invoicesystem.dto.InvoiceRequestDTO;
import com.project.invoicesystem.dto.InvoiceResponseDTO;
import com.project.invoicesystem.entity.Invoice;
import com.project.invoicesystem.mapper.InvoiceMapper;
import com.project.invoicesystem.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper mapper;

    @InjectMocks
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateInvoice() {
        InvoiceRequestDTO requestDTO = new InvoiceRequestDTO();
        requestDTO.setAmount(150.0);
        requestDTO.setDueDate(LocalDate.of(2023, 12, 31));

        Invoice mockInvoice = new Invoice(150.0, LocalDate.of(2023, 12, 31));
        mockInvoice.setId(1L);

        when(mapper.toEntity(requestDTO)).thenReturn(mockInvoice);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mockInvoice);
        System.out.println(mockInvoice);
        when(mapper.toDto(mockInvoice)).thenReturn(new InvoiceResponseDTO(1L, 150.0, 0.0, LocalDate.of(2023, 12, 31), InvoiceStatus.PENDING));

        InvoiceResponseDTO responseDTO = invoiceService.createInvoice(requestDTO);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(150.0, responseDTO.getAmount());
        assertEquals(InvoiceStatus.PENDING, responseDTO.getStatus());
    }
        // Arrange



    @Test
    void shouldGetAllInvoices() {
        Invoice invoice = new Invoice(200.0, LocalDate.of(2023, 12, 31));
        invoice.setId(1L);
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));
        when(mapper.toDto(invoice)).thenReturn(new InvoiceResponseDTO(1L, 200.0, 0.0, LocalDate.of(2023, 12, 31), InvoiceStatus.PENDING));

        List<InvoiceResponseDTO> response = invoiceService.getInvoices();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
    }

    @Test
    void shouldPayInvoiceSuccessfully() {
        Invoice invoice = new Invoice(100.0, LocalDate.of(2023, 12, 31));
        invoice.setId(1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(mapper.toDto(invoice)).thenReturn(new InvoiceResponseDTO(1L, 100.0, 100.0, LocalDate.of(2023, 12, 31), InvoiceStatus.PAID));

        InvoiceResponseDTO response = invoiceService.payInvoice(1L, 100.0);

        assertNotNull(response);
        assertEquals(100.0, response.getPaidAmount());
        assertEquals(InvoiceStatus.PAID, response.getStatus());
    }

    @Test
    void shouldThrowExceptionForInvalidInvoiceId() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> invoiceService.payInvoice(1L, 50.0));

        assertEquals("Invoice not found", exception.getMessage());
    }

    @Test
    void shouldProcessOverdueInvoices() {
        Invoice overdueInvoice = new Invoice(150.0, LocalDate.now().minusDays(20));
        overdueInvoice.setId(1L);

        when(invoiceRepository.findAll()).thenReturn(List.of(overdueInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(overdueInvoice); // Correctly stub the save method

        invoiceService.processOverdue(10.0, 10);

        verify(invoiceRepository, times(2)).save(any(Invoice.class)); // Verify save is called twice (original + new invoice)
    }


    @Test
    void shouldFindOverdueInvoicesUsingReflection() throws Exception {
        Invoice overdue = new Invoice(100.0, LocalDate.now().minusDays(15));
        overdue.setStatus(InvoiceStatus.PENDING);

        Invoice recent = new Invoice(200.0, LocalDate.now().minusDays(5));
        recent.setStatus(InvoiceStatus.PENDING);

        when(invoiceRepository.findAll()).thenReturn(List.of(overdue, recent));

        Method method = InvoiceService.class.getDeclaredMethod("findOverdueInvoices", int.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Invoice> overdueInvoices = (List<Invoice>) method.invoke(invoiceService, 10);

        assertNotNull(overdueInvoices);
        assertEquals(1, overdueInvoices.size());
        assertEquals(InvoiceStatus.PENDING, overdueInvoices.get(0).getStatus());
    }

    @Test
    void shouldThrowExceptionForInvalidStatus() {
        Invoice invoice = new Invoice(150.0, LocalDate.now());
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> invoiceService.payInvoice(1L, 50.0));

        assertEquals("Invalid or non-pending invoice", exception.getMessage());
    }
}
