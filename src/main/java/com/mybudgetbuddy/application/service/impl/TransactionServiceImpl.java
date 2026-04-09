package com.mybudgetbuddy.application.service.impl;

import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.infrastructure.database.DatabaseInitializer;
import com.mybudgetbuddy.infrastructure.database.DatabaseManager;
import com.mybudgetbuddy.model.PaymentMethod;
import com.mybudgetbuddy.model.RecurringFrequency;
import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQLite implementation of TransactionService for MVVM architecture.
 * Uses database persistence instead of file serialization.
 */
public class TransactionServiceImpl implements TransactionService {

    private final DatabaseManager databaseManager;

    public TransactionServiceImpl() {
        this.databaseManager = DatabaseManager.getInstance();
        
        // Initialize database schema
        DatabaseInitializer initializer = new DatabaseInitializer(databaseManager);
        initializer.initializeDatabase();
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
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
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
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
            
            setTransactionParameters(stmt, transaction);
            stmt.setString(15, transaction.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Transaction not found for update: " + transaction.getId());
            }
            
            return transaction;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    @Override
    public void deleteTransaction(String transactionId) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    @Override
    public Optional<Transaction> getTransactionById(String transactionId) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToTransaction(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
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
    @Override
    public BigDecimal getTotalExpenses() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            rs.next();
            return rs.getBigDecimal(1);
            
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
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBigDecimal(1);
            
        } catch (SQLException e) {
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
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBigDecimal(1);
            
        } catch (SQLException e) {
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
            SELECT c.name, COALESCE(SUM(t.amount), 0) as total
            FROM categories c
            LEFT JOIN transactions t ON c.id = t.category_id 
                AND t.transaction_date BETWEEN ? AND ?
                AND t.type = 'EXPENSE'
            GROUP BY c.id, c.name
            ORDER BY total DESC
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            Map<String, BigDecimal> categorySpending = new LinkedHashMap<>();
            
            while (rs.next()) {
                String categoryName = rs.getString("name");
                BigDecimal total = rs.getBigDecimal("total");
                if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
                    categorySpending.put(categoryName, total);
                }
            }
            
            return categorySpending;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get category spending", e);
        }
    }

    /**
     * Helper method to set transaction parameters in prepared statements
     */
    private void setTransactionParameters(PreparedStatement stmt, Transaction transaction) throws SQLException {
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
    }

    /**
     * Helper method to map ResultSet to Transaction object
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        
        transaction.setId(rs.getString("id"));
        transaction.setPlanId(rs.getString("plan_id"));
        transaction.setBudgetId(rs.getString("budget_id"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        
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