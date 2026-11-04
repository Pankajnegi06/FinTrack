package com.fintrack.service;

import com.fintrack.exception.BudgetExceededException;
import com.fintrack.exception.ExpenseNotFoundException;
import com.fintrack.exception.InvalidAmountException;
import com.fintrack.model.Budget;
import com.fintrack.model.Category;
import com.fintrack.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseManagerTest {
    private ExpenseManager manager;

    @BeforeEach
    void setUp() {
        manager = new ExpenseManager(new ArrayList<>(), new Budget());
    }

    @Test
    void addExpenseStoresExpense() throws InvalidAmountException {
        manager.addExpense(250.0, Category.FOOD, "Lunch");
        assertEquals(1, manager.getAllExpenses().size());
    }

    @Test
    void addExpenseWithNegativeAmountThrows() {
        assertThrows(InvalidAmountException.class, () -> manager.addExpense(-10, Category.FOOD, "Bad"));
    }

    @Test
    void addExpenseWithZeroAmountThrows() {
        assertThrows(InvalidAmountException.class, () -> manager.addExpense(0, Category.FOOD, "Zero"));
    }

    @Test
    void totalSpendingIsCorrect() throws InvalidAmountException {
        manager.addExpense(100, Category.FOOD, "A");
        manager.addExpense(200, Category.BILLS, "B");
        assertEquals(300.0, manager.getTotalSpending(), 0.001);
    }

    @Test
    void deleteExpenseRemovesIt() throws Exception {
        Expense e = manager.addExpense(100, Category.FOOD, "A");
        manager.deleteExpense(e.getId());
        assertTrue(manager.getAllExpenses().isEmpty());
    }

    @Test
    void deleteMissingExpenseThrows() {
        assertThrows(ExpenseNotFoundException.class, () -> manager.deleteExpense(99));
    }

    @Test
    void filterByCategoryReturnsOnlyMatching() throws InvalidAmountException {
        manager.addExpense(100, Category.FOOD, "A");
        manager.addExpense(200, Category.BILLS, "B");
        assertEquals(1, manager.filterByCategory(Category.FOOD).size());
    }

    @Test
    void filterByAmountRangeWorks() throws InvalidAmountException {
        manager.addExpense(50, Category.FOOD, "A");
        manager.addExpense(500, Category.FOOD, "B");
        assertEquals(1, manager.filterByAmountRange(100, 1000).size());
    }

    @Test
    void remainingBudgetIsBudgetMinusMonthlySpending() throws InvalidAmountException {
        manager.setBudget(1000);
        manager.addExpense(300, Category.FOOD, "A", LocalDate.now());
        assertEquals(700.0, manager.getRemainingBudget(YearMonth.now()), 0.001);
    }

    @Test
    void expensesFromOtherMonthDoNotCountForCurrentMonth() throws InvalidAmountException {
        manager.setBudget(1000);
        manager.addExpense(300, Category.FOOD, "Old", LocalDate.now().minusMonths(2));
        assertEquals(1000.0, manager.getRemainingBudget(YearMonth.now()), 0.001);
    }

    @Test
    void checkBudgetThrowsWhenExceeded() throws InvalidAmountException {
        manager.setBudget(100);
        manager.addExpense(150, Category.SHOPPING, "Big");
        BudgetExceededException ex = assertThrows(BudgetExceededException.class, () -> manager.checkBudget(YearMonth.now()));
        assertEquals(50.0, ex.getExceededBy(), 0.001);
    }

    @Test
    void sortedByAmountIsDescending() throws InvalidAmountException {
        manager.addExpense(10, Category.FOOD, "A");
        manager.addExpense(90, Category.FOOD, "B");
        manager.addExpense(50, Category.FOOD, "C");
        assertEquals(90.0, manager.getSortedByAmount().get(0).getAmount(), 0.001);
    }
}
