package com.fintrack.exception;

public class ExpenseNotFoundException extends Exception {
    public ExpenseNotFoundException(int id) {
        super("No expense found with ID " + id);
    }
}
