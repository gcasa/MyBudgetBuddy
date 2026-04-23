package com.mybudgetbuddy.application.service.impl;

import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.infrastructure.database.DatabaseInitializer;
import com.mybudgetbuddy.infrastructure.database.DatabaseManager;
import com.mybudgetbuddy.model.PaymentMethod;
import com.mybudgetbuddy.model.RecurringFrequency;
import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLite implementation of TransactionService for MVVM architecture.
 * Uses database persistence instead of file serialization.
 */
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());
    private final DatabaseManager databaseManager;

    public TransactionServiceImpl() {
        this.databaseManager = DatabaseManager.getInstance();
        
        // Initialize database schema
        DatabaseInitializer initializer = new DatabaseInitializer(databaseManager);
        initializer.initializeDatabase();
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        // Validate required fields
        if (transaction.getType() == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        
        if (transaction.getId() == null || transaction.getId().isEmpty()) {
            transaction.setId(UUID.randomUUID().toString());
        }
        
        String sql = """
            INSERT INTO transactions (
                id, plan_id, budget_id, amount, type, payment_method, 
                transaction_date, description, category_id, account_id,
                recurring_frequency, is_recurring, parent_transaction_id,
                next_occurrence, end_date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setTransactionParameters(stmt, transaction);
            stmt.executeUpdate();
            
            return transaction;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create transaction with ID: " + transaction.getId(), e);
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        if (transaction.getId() == null) {
            throw new IllegalArgumentException("Transaction ID is required for update");
        }
        
        String sql = """
            UPDATE transactions SET
                plan_id = ?, budget_id = ?, amount = ?, type = ?, payment_method = ?,
                transaction_date = ?, description = ?, category_id = ?, account_id = ?,
                recurring_frequency = ?, is_recurring = ?, parent_transaction_id = ?,
                next_occurrence = ?, end_date = ?, updated_date = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // UPDATE params: 14 SET columns + WHERE id (pos 15)
            // setTransactionParameters is for INSERT (id at pos 1), so bind directly here
            stmt.setString(1, transaction.getPlanId());
            stmt.setString(2, transaction.getBudgetId());
            stmt.setBigDecimal(3, transaction.getAmount());
            stmt.setString(4, transaction.getType() != null ? transaction.getType().name() : null);
            stmt.setString(5, transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null);
            stmt.setDate(6, transaction.getTransactionDate() != null ? Date.valueOf(transaction.getTransactionDate()) : null);
            stmt.setString(7, transaction.getDescription());
            stmt.setString(8, transaction.getCategoryId());
            stmt.setString(9, transaction.getAccountId());
            stmt.setString(10, transaction.getRecurringFrequency() != null ? transaction.getRecurringFrequency().name() : null);
            stmt.setBoolean(11, transaction.getIsRecurring() != null ? transaction.getIsRecurring() : false);
            stmt.setString(12, transaction.getParentTransactionId());
            stmt.setDate(13, transaction.getNextOccurrence() != null ? Date.valueOf(transaction.getNextOccurrence()) : null);
            stmt.setDate(14, transaction.getEndDate() != null ? Date.valueOf(transaction.getEndDate()) : null);
            stmt.setString(15, transaction.getId());  // WHERE id = ?
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Transaction not found for update: " + transaction.getId());
            }
            
            return transaction;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update transaction with ID: " + transaction.getId(), e);
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    @Override
    public void deleteTransaction(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }
        
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete transaction with ID: " + transactionId, e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    @Override
    public Optional<Transaction> getTransactionById(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get transaction by ID: " + transactionId, e);
            throw new RuntimeException("Failed to get transaction by ID", e);
        }
    }

    @Override
    public List<Transaction> getAllTransactions() {
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<Transaction> transactions = new ArrayList<>();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            
            return transactions;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all transactions", e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT * FROM transactions 
            WHERE transaction_date BETWEEN ? AND ? 
            ORDER BY transaction_date DESC
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            List<Transaction> transactions = new ArrayList<>();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            
            return transactions;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions by date range", e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByCategory(String categoryId) {
        String sql = """
            SELECT * FROM transactions 
            WHERE category_id = ? 
            ORDER BY transaction_date DESC
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryId);
            
            ResultSet rs = stmt.executeQuery();
            List<Transaction> transactions = new ArrayList<>();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            
            return transactions;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions by category", e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByType(TransactionType type) {
        String sql = """
            SELECT * FROM transactions 
            WHERE type = ? 
            ORDER BY transaction_date DESC
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.name());
            
            ResultSet rs = stmt.executeQuery();
            List<Transaction> transactions = new ArrayList<>();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            
            return transactions;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions by type", e);
        }
    }

    @Override
    public BigDecimal getTotalIncome() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            rs.next();
            return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get total income", e);
            throw new RuntimeException("Failed to get total income", e);
        }
    }

    @Override
    public BigDecimal getTotalExpenses() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            rs.next();
            return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total expenses", e);
        }
    }

    @Override
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpenses());
    }

    @Override
    public BigDecimal getTotalIncomeForPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) FROM transactions 
            WHERE type = 'INCOME' AND transaction_date BETWEEN ? AND ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get total income for period", e);
            throw new RuntimeException("Failed to get total income for period", e);
        }
    }

    @Override
    public BigDecimal getTotalExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) FROM transactions 
            WHERE type = 'EXPENSE' AND transaction_date BETWEEN ? AND ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get total expenses for period", e);
            throw new RuntimeException("Failed to get total expenses for period", e);
        }
    }

    @Override
    public BigDecimal getBalanceForPeriod(LocalDate startDate, LocalDate endDate) {
        return getTotalIncomeForPeriod(startDate, endDate)
                .subtract(getTotalExpensesForPeriod(startDate, endDate));
    }

    @Override
    public Map<String, BigDecimal> getCategorySpending(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT c.id, COALESCE(SUM(t.amount), 0) as total
            FROM categories c
            LEFT JOIN transactions t ON c.id = t.category_id 
                AND t.transaction_date BETWEEN ? AND ?
                AND t.type = 'EXPENSE'
            GROUP BY c.id
            ORDER BY total DESC
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                Map<String, BigDecimal> categorySpending = new LinkedHashMap<>();
                
                while (rs.next()) {
                    String categoryId = rs.getString("id");
                    BigDecimal total = rs.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP);
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        categorySpending.put(categoryId, total);
                    }
                }
                
                return categorySpending;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get category spending", e);
            throw new RuntimeException("Failed to get category spending", e);
        }
    }

    @Override
    public BigDecimal getTotalIncome(String planId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) FROM transactions 
            WHERE type = 'INCOME' AND plan_id = ? AND transaction_date BETWEEN ? AND ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get total income for plan and period", e);
            throw new RuntimeException("Failed to get total income for plan and period", e);
        }
    }

    @Override
    public BigDecimal getTotalExpenses(String planId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) FROM transactions 
            WHERE type = 'EXPENSE' AND plan_id = ? AND transaction_date BETWEEN ? AND ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get total expenses for plan and period", e);
            throw new RuntimeException("Failed to get total expenses for plan and period", e);
        }
    }

    @Override
    public List<Transaction> getRecentTransactions(String planId, int limit) {
        String sql = """
            SELECT * FROM transactions 
            WHERE plan_id = ? 
            ORDER BY transaction_date DESC, created_date DESC
            LIMIT ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                return transactions;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get recent transactions for plan", e);
            throw new RuntimeException("Failed to get recent transactions for plan", e);
        }
    }

    /**
     * Helper method to set transaction parameters in prepared statements
     */
    private void setTransactionParameters(PreparedStatement stmt, Transaction transaction) throws SQLException {
        stmt.setString(1, transaction.getId());  // id
        stmt.setString(2, transaction.getPlanId());  // plan_id
        stmt.setString(3, transaction.getBudgetId());  // budget_id
        stmt.setBigDecimal(4, transaction.getAmount());  // amount
        stmt.setString(5, transaction.getType() != null ? transaction.getType().name() : null);  // type
        stmt.setString(6, transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null);  // payment_method
        stmt.setDate(7, transaction.getTransactionDate() != null ? Date.valueOf(transaction.getTransactionDate()) : null);  // transaction_date
        stmt.setString(8, transaction.getDescription());  // description
        stmt.setString(9, transaction.getCategoryId());  // category_id
        stmt.setString(10, transaction.getAccountId());  // account_id
        stmt.setString(11, transaction.getRecurringFrequency() != null ? transaction.getRecurringFrequency().name() : null);  // recurring_frequency
        stmt.setBoolean(12, transaction.getIsRecurring() != null ? transaction.getIsRecurring() : false);  // is_recurring
        stmt.setString(13, transaction.getParentTransactionId());  // parent_transaction_id
        stmt.setDate(14, transaction.getNextOccurrence() != null ? Date.valueOf(transaction.getNextOccurrence()) : null);  // next_occurrence
        stmt.setDate(15, transaction.getEndDate() != null ? Date.valueOf(transaction.getEndDate()) : null);  // end_date
    }

    /**
     * Helper method to map ResultSet to Transaction object
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        
        transaction.setId(rs.getString("id"));
        transaction.setPlanId(rs.getString("plan_id"));
        transaction.setBudgetId(rs.getString("budget_id"));
        BigDecimal amount = rs.getBigDecimal("amount");
        transaction.setAmount(amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : null);
        
        String type = rs.getString("type");
        if (type != null) {
            transaction.setType(TransactionType.valueOf(type));
        }
        
        String paymentMethod = rs.getString("payment_method");
        if (paymentMethod != null) {
            try {
                // Assuming PaymentMethod is an enum - if not, just set as string
                transaction.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
            } catch (IllegalArgumentException e) {
                // Handle case where enum value doesn't exist
                System.err.println("Unknown payment method: " + paymentMethod);
            }
        }
        
        Date date = rs.getDate("transaction_date");
        if (date != null) {
            transaction.setTransactionDate(date.toLocalDate());
        }
        
        transaction.setDescription(rs.getString("description"));
        transaction.setCategoryId(rs.getString("category_id"));
        transaction.setAccountId(rs.getString("account_id"));
        
        String recurringFreq = rs.getString("recurring_frequency");
        if (recurringFreq != null) {
            try {
                // Assuming RecurringFrequency is an enum - if not, adapt accordingly
                transaction.setRecurringFrequency(RecurringFrequency.valueOf(recurringFreq));
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown recurring frequency: " + recurringFreq);
            }
        }
        
        transaction.setIsRecurring(rs.getBoolean("is_recurring"));
        transaction.setParentTransactionId(rs.getString("parent_transaction_id"));
        
        Date nextOccurrence = rs.getDate("next_occurrence");
        if (nextOccurrence != null) {
            transaction.setNextOccurrence(nextOccurrence.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            transaction.setEndDate(endDate.toLocalDate());
        }
        
        return transaction;
    }
}