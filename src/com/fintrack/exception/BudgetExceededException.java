package com.fintrack.exception;

public class BudgetExceededException extends Exception {
    private final double exceededBy;

    public BudgetExceededException(String message, double exceededBy) {
        super(message);
        this.exceededBy = exceededBy;
    }

    public double getExceededBy() {
        return exceededBy;
    }
}
