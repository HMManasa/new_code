package com.project.invoicesystem.common;

public enum InvoiceStatus {
    PENDING, PAID, VOID;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
