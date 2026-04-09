package com.mybudgetbuddy.service;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {

    private static final String DATA_FILE =
            Paths.get(System.getProperty("user.home"), ".mybudgetbuddy_data.ser").toString();

    private List<Transaction> transactions;

    public BudgetService() {
        transactions = new ArrayList<>();
        load();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        save();
    }

    public void updateTransaction(Transaction updated) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(updated.getId())) {
                transactions.set(i, updated);
                break;
            }
        }
        save();
    }

    public void deleteTransaction(String id) {
        transactions.removeIf(t -> t.getId().equals(id));
        save();
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                transactions = (List<Transaction>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            // Start fresh if file is corrupt or unreadable
            transactions = new ArrayList<>();
        }
    }

    private void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save budget data: " + e.getMessage(), e);
        }
    }
}
