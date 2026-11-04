package com.fintrack.model;

import java.time.LocalDate;

public class RecurringExpense extends Expense {
    private int intervalDays;

    public RecurringExpense(int id, double amount, Category category, String description, LocalDate date, int intervalDays) {
        super(id, amount, category, description, date);
        this.intervalDays = intervalDays;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " (repeats every " + intervalDays + " days)";
    }

    @Override
    public String toFileString() {
        return "RECURRING|" + getId() + "|" + getAmount() + "|" + getCategory().name() + "|" + getDescription() + "|" + getDate() + "|" + intervalDays;
    }
}
