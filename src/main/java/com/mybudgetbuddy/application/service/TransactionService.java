package com.mybudgetbuddy.application.service;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing financial transactions.
 * Focused on core MVVM functionality without legacy complexity.
 */
public interface TransactionService {
    
    // Core CRUD operations
    Transaction createTransaction(Transaction transaction);
    Transaction updateTransaction(Transaction transaction);
    void deleteTransaction(String transactionId);
    Optional<Transaction> getTransactionById(String transactionId);
    
    // Query operations for UI
    List<Transaction> getAllTransactions();
    List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);
    List<Transaction> getTransactionsByCategory(String categoryId);
    List<Transaction> getTransactionsByType(TransactionType type);
    
    // Financial calculations for UI summary
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpenses(); 
    BigDecimal getBalance();
    BigDecimal getTotalIncomeForPeriod(LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalExpensesForPeriod(LocalDate startDate, LocalDate endDate);
    BigDecimal getBalanceForPeriod(LocalDate startDate, LocalDate endDate);
    Map<String, BigDecimal> getCategorySpending(LocalDate startDate, LocalDate endDate);
    
    // Plan-specific calculations for dashboard
    BigDecimal getTotalIncome(String planId, LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalExpenses(String planId, LocalDate startDate, LocalDate endDate);
    List<Transaction> getRecentTransactions(String planId, int limit);
}