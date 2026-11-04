package com.fintrack.model;

import java.time.LocalDate;

public class Expense extends Transaction {
    private Category category;
    private String description;

    public Expense(int id, double amount, Category category, String description, LocalDate date) {
        super(id, amount, date);
        this.category = category;
        this.description = description;
    }

    public Expense(int id, double amount, Category category, String description) {
        this(id, amount, category, description, LocalDate.now());
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getSummary() {
        return String.format("%-4d %-12s %-14s %10.2f  %s", getId(), getDate(), category.getLabel(), getAmount(), description);
    }

    @Override
    public String toFileString() {
        return "EXPENSE|" + getId() + "|" + getAmount() + "|" + category.name() + "|" + description + "|" + getDate() + "|0";
    }
}
