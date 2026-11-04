package com.fintrack.model;

import java.time.LocalDate;

public abstract class Transaction {
    private final int id;
    private double amount;
    private LocalDate date;

    public Transaction(int id, double amount, LocalDate date) {
        this.id = id;
        this.amount = amount;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public abstract String getSummary();

    public abstract String toFileString();
}
