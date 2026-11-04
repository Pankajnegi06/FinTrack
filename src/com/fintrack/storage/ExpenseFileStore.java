package com.fintrack.storage;

import com.fintrack.model.Category;
import com.fintrack.model.Expense;
import com.fintrack.model.RecurringExpense;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseFileStore implements DataStore<Expense> {
    private final String fileName;

    public ExpenseFileStore(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void saveAll(List<Expense> items) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Expense e : items) {
                writer.write(e.toFileString());
                writer.newLine();
            }
        }
    }

    @Override
    public List<Expense> loadAll() throws IOException {
        List<Expense> list = new ArrayList<>();
        File file = new File(fileName);
        if (!file.exists()) {
            return list;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length < 7) {
                    continue;
                }
                String type = parts[0];
                int id = Integer.parseInt(parts[1]);
                double amount = Double.parseDouble(parts[2]);
                Category category = Category.fromString(parts[3]);
                String description = parts[4];
                LocalDate date = LocalDate.parse(parts[5]);
                int interval = Integer.parseInt(parts[6]);
                if (type.equals("RECURRING")) {
                    list.add(new RecurringExpense(id, amount, category, description, date, interval));
                } else {
                    list.add(new Expense(id, amount, category, description, date));
                }
            }
        }
        return list;
    }
}
