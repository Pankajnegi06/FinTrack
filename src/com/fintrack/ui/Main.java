package com.fintrack.ui;

import com.fintrack.exception.BudgetExceededException;
import com.fintrack.exception.ExpenseNotFoundException;
import com.fintrack.exception.InvalidAmountException;
import com.fintrack.model.Budget;
import com.fintrack.model.Category;
import com.fintrack.model.Expense;
import com.fintrack.service.ExpenseManager;
import com.fintrack.storage.BudgetFileStore;
import com.fintrack.storage.ExpenseFileStore;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static ExpenseManager manager;
    private static ExpenseFileStore expenseStore;
    private static BudgetFileStore budgetStore;

    public static void main(String[] args) {
        expenseStore = new ExpenseFileStore("expenses.txt");
        budgetStore = new BudgetFileStore("budget.txt");
        try {
            List<Expense> saved = expenseStore.loadAll();
            Budget budget = budgetStore.load();
            manager = new ExpenseManager(saved, budget);
            System.out.println("Loaded " + saved.size() + " saved expense(s).");
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not read saved data, starting fresh: " + e.getMessage());
            manager = new ExpenseManager(new java.util.ArrayList<>(), new Budget());
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1:
                    addExpense();
                    break;
                case 2:
                    addRecurringExpense();
                    break;
                case 3:
                    viewExpenses();
                    break;
                case 4:
                    deleteExpense();
                    break;
                case 5:
                    filterExpenses();
                    break;
                case 6:
                    showTotal();
                    break;
                case 7:
                    setBudget();
                    break;
                case 8:
                    showRemainingBudget();
                    break;
                case 9:
                    System.out.println(manager.generateReport());
                    break;
                case 10:
                    saveData();
                    break;
                case 0:
                    saveData();
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select from the menu.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=========== FinTrack ===========");
        System.out.println("1.  Add expense");
        System.out.println("2.  Add recurring expense");
        System.out.println("3.  View all expenses");
        System.out.println("4.  Delete expense");
        System.out.println("5.  Filter expenses");
        System.out.println("6.  Show total spending");
        System.out.println("7.  Set monthly budget");
        System.out.println("8.  Show remaining budget");
        System.out.println("9.  Generate report");
        System.out.println("10. Save data");
        System.out.println("0.  Save and exit");
        System.out.println("================================");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static Category readCategory() {
        System.out.println("Categories:");
        Category[] all = Category.values();
        for (int i = 0; i < all.length; i++) {
            System.out.println("  " + (i + 1) + ". " + all[i].getLabel());
        }
        while (true) {
            int n = readInt("Choose category number: ");
            if (n >= 1 && n <= all.length) {
                return all[n - 1];
            }
            System.out.println("Choose a number between 1 and " + all.length + ".");
        }
    }

    private static LocalDate readDate() {
        while (true) {
            System.out.print("Date (yyyy-mm-dd, press Enter for today): ");
            String text = sc.nextLine().trim();
            if (text.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Example: 2026-10-31");
            }
        }
    }

    private static String readDescription() {
        System.out.print("Description: ");
        return sc.nextLine().trim().replace("|", "/");
    }

    private static void addExpense() {
        try {
            double amount = readDouble("Amount: ");
            Category category = readCategory();
            String description = readDescription();
            LocalDate date = readDate();
            Expense e = manager.addExpense(amount, category, description, date);
            System.out.println("Expense added with ID " + e.getId());
            warnIfOverBudget(YearMonth.from(date));
        } catch (InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void addRecurringExpense() {
        try {
            double amount = readDouble("Amount: ");
            Category category = readCategory();
            String description = readDescription();
            LocalDate date = readDate();
            int interval = readInt("Repeats every how many days: ");
            Expense e = manager.addRecurringExpense(amount, category, description, date, interval);
            System.out.println("Recurring expense added with ID " + e.getId());
            warnIfOverBudget(YearMonth.from(date));
        } catch (InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void warnIfOverBudget(YearMonth month) {
        try {
            manager.checkBudget(month);
        } catch (BudgetExceededException e) {
            System.out.printf("WARNING: %s by %.2f%n", e.getMessage(), e.getExceededBy());
        }
    }

    private static void printList(List<Expense> list) {
        if (list.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }
        System.out.println(String.format("%-4s %-12s %-14s %10s  %s", "ID", "Date", "Category", "Amount", "Description"));
        System.out.println("-------------------------------------------------------------");
        list.forEach(e -> System.out.println(e.getSummary()));
    }

    private static void viewExpenses() {
        System.out.println("Sort by: 1. Date  2. Amount (highest first)  3. As added");
        int choice = readInt("Choice: ");
        if (choice == 1) {
            printList(manager.getSortedByDate());
        } else if (choice == 2) {
            printList(manager.getSortedByAmount());
        } else {
            printList(manager.getAllExpenses());
        }
    }

    private static void deleteExpense() {
        int id = readInt("Enter expense ID to delete: ");
        try {
            manager.deleteExpense(id);
            System.out.println("Expense deleted.");
        } catch (ExpenseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void filterExpenses() {
        System.out.println("Filter by: 1. Category  2. Month  3. Amount range");
        int choice = readInt("Choice: ");
        switch (choice) {
            case 1:
                printList(manager.filterByCategory(readCategory()));
                break;
            case 2:
                System.out.print("Month (yyyy-mm): ");
                try {
                    YearMonth month = YearMonth.parse(sc.nextLine().trim());
                    printList(manager.filterByMonth(month));
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid month format. Example: 2026-10");
                }
                break;
            case 3:
                double min = readDouble("Minimum amount: ");
                double max = readDouble("Maximum amount: ");
                printList(manager.filterByAmountRange(min, max));
                break;
            default:
                System.out.println("Invalid filter choice.");
        }
    }

    private static void showTotal() {
        System.out.printf("Total spending: %.2f%n", manager.getTotalSpending());
        System.out.printf("Spending this month: %.2f%n", manager.getMonthlySpending(YearMonth.now()));
    }

    private static void setBudget() {
        try {
            double limit = readDouble("Enter monthly budget: ");
            manager.setBudget(limit);
            System.out.printf("Monthly budget set to %.2f%n", limit);
        } catch (InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void showRemainingBudget() {
        if (!manager.getBudget().isSet()) {
            System.out.println("Budget is not set yet. Use option 7 first.");
            return;
        }
        YearMonth now = YearMonth.now();
        System.out.printf("Monthly budget : %.2f%n", manager.getBudget().getMonthlyLimit());
        System.out.printf("Spent          : %.2f%n", manager.getMonthlySpending(now));
        System.out.printf("Remaining      : %.2f%n", manager.getRemainingBudget(now));
        warnIfOverBudget(now);
    }

    private static void saveData() {
        try {
            expenseStore.saveAll(manager.getAllExpenses());
            budgetStore.save(manager.getBudget());
            System.out.println("Data saved.");
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }
}
