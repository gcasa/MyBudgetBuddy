package com.mybudgetbuddy.application.service.impl;

import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple implementation of TransactionService for MVVM architecture.
 * Focuses on core functionality without legacy complexity.
 */
public class TransactionServiceImpl implements TransactionService {

    private static final String DATA_FILE =
            Paths.get(System.getProperty("user.home"), ".mybudgetbuddy_data.ser").toString();

    private final Map<String, Transaction> transactions = new HashMap<>();

    public TransactionServiceImpl() {
        load();
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getId() == null || transaction.getId().isEmpty()) {
            transaction.setId(UUID.randomUUID().toString());
        }
        
        transactions.put(transaction.getId(), transaction);
        save();
        return transaction;
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        if (transaction.getId() == null || !transactions.containsKey(transaction.getId())) {
            throw new IllegalArgumentException("Transaction ID is required for update");
        }
        
        transactions.put(transaction.getId(), transaction);
        save();
        return transaction;
    }

    @Override
    public void deleteTransaction(String transactionId) {
        transactions.remove(transactionId);
        save();
    }

    @Override
    public Optional<Transaction> getTransactionById(String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values()).stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList();
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactions.values().stream()
                .filter(t -> !t.getTransactionDate().isBefore(startDate))
                .filter(t -> !t.getTransactionDate().isAfter(endDate))
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList();
    }

    @Override
    public List<Transaction> getTransactionsByCategory(String categoryId) {
        return transactions.values().stream()
                .filter(t -> categoryId.equals(t.getCategoryId()))
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList();
    }

    @Override
    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactions.values().stream()
                .filter(t -> type == t.getType())
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList();
    }

    @Override
    public BigDecimal getTotalIncome() {
        return transactions.values().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalExpenses() {
        return transactions.values().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpenses());
    }

    @Override
    public BigDecimal getTotalIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        return getTransactionsByDateRange(startDate, endDate).stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        return getTransactionsByDateRange(startDate, endDate).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getBalanceForPeriod(LocalDate startDate, LocalDate endDate) {
        BigDecimal income = getTotalIncomeForPeriod(startDate, endDate);
        BigDecimal expenses = getTotalExpensesForPeriod(startDate, endDate);
        return income.subtract(expenses);
    }

    @Override
    public Map<String, BigDecimal> getCategorySpending(LocalDate startDate, LocalDate endDate) {
        return getTransactionsByDateRange(startDate, endDate).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    // Data persistence methods
    
    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                // Convert from old List<Transaction> format
                List<Transaction> transactionList = (List<Transaction>) obj;
                transactions.clear();
                for (Transaction t : transactionList) {
                    if (t.getId() == null) {
                        t.setId(UUID.randomUUID().toString());
                    }
                    transactions.put(t.getId(), t);
                }
                save(); // Convert to new format
            } else if (obj instanceof Map) {
                // New Map<String, Transaction> format
                Map<String, Transaction> loadedTransactions = (Map<String, Transaction>) obj;
                transactions.clear();
                transactions.putAll(loadedTransactions);
            }
        } catch (IOException | ClassNotFoundException e) {
            // Start fresh if file is corrupt or unreadable
            transactions.clear();
        }
    }

    private void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            System.err.println("Failed to save transaction data: " + e.getMessage());
        }
    }
}