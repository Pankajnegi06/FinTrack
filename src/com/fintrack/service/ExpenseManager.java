package com.fintrack.service;

import com.fintrack.exception.BudgetExceededException;
import com.fintrack.exception.ExpenseNotFoundException;
import com.fintrack.exception.InvalidAmountException;
import com.fintrack.model.Budget;
import com.fintrack.model.Category;
import com.fintrack.model.Expense;
import com.fintrack.model.RecurringExpense;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseManager implements ReportGenerator {
    private final List<Expense> expenses;
    private final Budget budget;
    private int nextId;

    public ExpenseManager(List<Expense> existing, Budget budget) {
        this.expenses = new ArrayList<>(existing);
        this.budget = budget;
        this.nextId = existing.stream().mapToInt(Expense::getId).max().orElse(0) + 1;
    }

    private void checkAmount(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }

    public Expense addExpense(double amount, Category category, String description) throws InvalidAmountException {
        return addExpense(amount, category, description, LocalDate.now());
    }

    public Expense addExpense(double amount, Category category, String description, LocalDate date) throws InvalidAmountException {
        checkAmount(amount);
        Expense e = new Expense(nextId++, amount, category, description, date);
        expenses.add(e);
        return e;
    }

    public RecurringExpense addRecurringExpense(double amount, Category category, String description, LocalDate date, int intervalDays) throws InvalidAmountException {
        checkAmount(amount);
        if (intervalDays <= 0) {
            throw new InvalidAmountException("Interval must be at least 1 day.");
        }
        RecurringExpense e = new RecurringExpense(nextId++, amount, category, description, date, intervalDays);
        expenses.add(e);
        return e;
    }

    public void deleteExpense(int id) throws ExpenseNotFoundException {
        boolean removed = expenses.removeIf(e -> e.getId() == id);
        if (!removed) {
            throw new ExpenseNotFoundException(id);
        }
    }

    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }

    public List<Expense> getSortedByDate() {
        return expenses.stream()
                .sorted(Comparator.comparing(Expense::getDate))
                .collect(Collectors.toList());
    }

    public List<Expense> getSortedByAmount() {
        return expenses.stream()
                .sorted(Comparator.comparingDouble(Expense::getAmount).reversed())
                .collect(Collectors.toList());
    }

    public List<Expense> filterByCategory(Category category) {
        return expenses.stream()
                .filter(e -> e.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Expense> filterByMonth(YearMonth month) {
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .collect(Collectors.toList());
    }

    public List<Expense> filterByAmountRange(double min, double max) {
        return expenses.stream()
                .filter(e -> e.getAmount() >= min && e.getAmount() <= max)
                .collect(Collectors.toList());
    }

    public double getTotalSpending() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public double getMonthlySpending(YearMonth month) {
        return filterByMonth(month).stream().mapToDouble(Expense::getAmount).sum();
    }

    public Map<Category, Double> getTotalsByCategory() {
        Map<Category, Double> totals = new EnumMap<>(Category.class);
        for (Expense e : expenses) {
            totals.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        return totals;
    }

    public void setBudget(double limit) throws InvalidAmountException {
        checkAmount(limit);
        budget.setMonthlyLimit(limit);
    }

    public Budget getBudget() {
        return budget;
    }

    public double getRemainingBudget(YearMonth month) {
        return budget.getRemaining(getMonthlySpending(month));
    }

    public void checkBudget(YearMonth month) throws BudgetExceededException {
        if (!budget.isSet()) {
            return;
        }
        double remaining = getRemainingBudget(month);
        if (remaining < 0) {
            throw new BudgetExceededException("Budget exceeded for " + month, -remaining);
        }
    }

    @Override
    public String generateReport() {
        YearMonth now = YearMonth.now();
        StringBuilder sb = new StringBuilder();
        sb.append("========== FINTRACK SPENDING REPORT ==========\n");
        sb.append(String.format("Total expenses recorded : %d%n", expenses.size()));
        sb.append(String.format("Total spending          : %.2f%n", getTotalSpending()));
        sb.append(String.format("Spending this month     : %.2f%n", getMonthlySpending(now)));
        if (budget.isSet()) {
            sb.append(String.format("Monthly budget          : %.2f%n", budget.getMonthlyLimit()));
            sb.append(String.format("Remaining budget        : %.2f%n", getRemainingBudget(now)));
        } else {
            sb.append("Monthly budget          : not set\n");
        }
        sb.append("\nSpending by category:\n");
        double total = getTotalSpending();
        getTotalsByCategory().entrySet().stream()
                .sorted(Map.Entry.<Category, Double>comparingByValue().reversed())
                .forEach(en -> sb.append(String.format("  %-14s %10.2f  (%.1f%%)%n",
                        en.getKey().getLabel(), en.getValue(), total == 0 ? 0 : en.getValue() * 100 / total)));
        expenses.stream()
                .max(Comparator.comparingDouble(Expense::getAmount))
                .ifPresent(e -> sb.append(String.format("%nHighest expense: %.2f on %s (%s)%n",
                        e.getAmount(), e.getCategory().getLabel(), e.getDescription())));
        sb.append("==============================================\n");
        return sb.toString();
    }
}
