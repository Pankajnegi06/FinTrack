package com.fintrack.storage;

import com.fintrack.model.Budget;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BudgetFileStore {
    private final String fileName;

    public BudgetFileStore(String fileName) {
        this.fileName = fileName;
    }

    public void save(Budget budget) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(String.valueOf(budget.getMonthlyLimit()));
        }
    }

    public Budget load() throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            return new Budget();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                return new Budget();
            }
            return new Budget(Double.parseDouble(line.trim()));
        }
    }
}
