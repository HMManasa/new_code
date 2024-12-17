package com.project.invoicesystem.entity;


import com.project.invoicesystem.common.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double amount;
    private double paidAmount;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;


    //public Invoice() {}

    public Invoice(double amount, LocalDate dueDate) {
        this.amount = amount;
        this.paidAmount = 0;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.PENDING;
    }
}
