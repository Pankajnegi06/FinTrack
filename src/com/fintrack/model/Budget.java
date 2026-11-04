package com.fintrack.model;

public class Budget {
    private double monthlyLimit;

    public Budget() {
        this.monthlyLimit = 0;
    }

    public Budget(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getRemaining(double spent) {
        return monthlyLimit - spent;
    }

    public boolean isSet() {
        return monthlyLimit > 0;
    }
}
