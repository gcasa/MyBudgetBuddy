package com.mybudgetbuddy.infrastructure.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes the SQLite database schema.
 * Creates all necessary tables for the MyBudgetBuddy application.
 */
public class DatabaseInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    private final DatabaseManager databaseManager;
    
    public DatabaseInitializer(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Initialize all database tables
     */
    public void initializeDatabase() {
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create tables in dependency order
            createUsersTable(stmt);
            createFinancialPlansTable(stmt);
            createCategoriesTable(stmt);
            createTransactionsTable(stmt);
            createBudgetsTable(stmt);
            createGoalsTable(stmt);
            createScenariosTable(stmt);
            createRecommendationsTable(stmt);
            createReportsTable(stmt);
            
            LOGGER.info("Database schema initialized successfully");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
    
    private void createUsersTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                created_date DATE NOT NULL
            )
        """;
        stmt.execute(sql);
    }
    
    private void createFinancialPlansTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS financial_plans (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                type TEXT NOT NULL,
                start_date DATE NOT NULL,
                end_date DATE,
                status TEXT NOT NULL,
                total_income DECIMAL(15,2),
                total_expenses DECIMAL(15,2),
                target_savings DECIMAL(15,2),
                emergency_fund_target DECIMAL(15,2),
                created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;
        stmt.execute(sql);
    }
    
    private void createCategoriesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS categories (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT,
                color TEXT,
                type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE'))
            )
        """;
        stmt.execute(sql);
        
        // Insert default categories if they don't exist
        insertDefaultCategories(stmt);
    }
    
    private void createTransactionsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS transactions (
                id TEXT PRIMARY KEY,
                plan_id TEXT,
                budget_id TEXT,
                amount DECIMAL(15,2) NOT NULL,
                type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER', 'INVESTMENT', 'REFUND')),
                payment_method TEXT,
                transaction_date DATE NOT NULL,
                description TEXT,
                category_id TEXT,
                account_id TEXT,
                recurring_frequency TEXT,
                is_recurring BOOLEAN DEFAULT FALSE,
                parent_transaction_id TEXT,
                next_occurrence DATE,
                end_date DATE,
                created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (plan_id) REFERENCES financial_plans(id),
                FOREIGN KEY (category_id) REFERENCES categories(id),
                FOREIGN KEY (parent_transaction_id) REFERENCES transactions(id)
            )
        """;
        stmt.execute(sql);
    }
    
    private void createBudgetsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS budgets (
                id TEXT PRIMARY KEY,
                plan_id TEXT,
                name TEXT NOT NULL,
                description TEXT,
                amount DECIMAL(15,2) NOT NULL,
                start_date DATE NOT NULL,
                end_date DATE,
                created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (plan_id) REFERENCES financial_plans(id)
            )
        """;
        stmt.execute(sql);
    }
    
    private void createGoalsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS goals (
                id TEXT PRIMARY KEY,
                plan_id TEXT,
                name TEXT NOT NULL,
                description TEXT,
                type TEXT,
                target_amount DECIMAL(15,2) NOT NULL,
                current_amount DECIMAL(15,2) DEFAULT 0,
                target_date DATE,
                priority TEXT DEFAULT 'MEDIUM',
                status TEXT DEFAULT 'ACTIVE',
                monthly_contribution DECIMAL(15,2),
                created_date DATE NOT NULL,
                last_updated DATE NOT NULL,
                FOREIGN KEY (plan_id) REFERENCES financial_plans(id)
            )
        """;
        stmt.execute(sql);
    }
    
    private void createScenariosTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS scenarios (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT,
                parameters TEXT,
                created_date DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;
        stmt.execute(sql);
    }
    
    private void createRecommendationsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS recommendations (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT,
                type TEXT,
                priority TEXT,
                created_date DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;
        stmt.execute(sql);
    }
    
    private void createReportsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS reports (
                id TEXT PRIMARY KEY,
                plan_id TEXT,
                user_id TEXT,
                name TEXT NOT NULL,
                description TEXT,
                type TEXT,
                format TEXT,
                start_date DATE,
                end_date DATE,
                included_categories TEXT,
                included_goals TEXT,
                included_budgets TEXT,
                include_graphs BOOLEAN DEFAULT TRUE,
                include_recommendations BOOLEAN DEFAULT TRUE,
                include_forecast BOOLEAN DEFAULT FALSE,
                content TEXT,
                data_json TEXT,
                chart_urls TEXT,
                pdf_content BLOB,
                file_path TEXT,
                summary_stats TEXT,
                key_insights TEXT,
                action_items TEXT,
                status TEXT DEFAULT 'PENDING',
                generated_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                last_accessed_date DATETIME,
                expiry_date DATETIME,
                file_size_bytes INTEGER DEFAULT 0,
                generated_by TEXT,
                page_count INTEGER DEFAULT 0,
                template_version TEXT
            )
        """;
        stmt.execute(sql);
    }
    
    private void insertDefaultCategories(Statement stmt) throws SQLException {
        // Check if categories exist first
        String checkSql = "SELECT COUNT(*) FROM categories";
        var rs = stmt.executeQuery(checkSql);
        rs.next();
        if (rs.getInt(1) > 0) {
            return; // Categories already exist
        }
        
        // Insert default income categories
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('income-salary', 'Salary', 'Regular employment income', '#4CAF50', 'INCOME')");
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('income-freelance', 'Freelance', 'Freelance and contract work', '#8BC34A', 'INCOME')");
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('income-investment', 'Investment', 'Investment returns and dividends', '#009688', 'INCOME')");
        
        // Insert default expense categories
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('expense-food', 'Food & Dining', 'Groceries, restaurants, food delivery', '#FF9800', 'EXPENSE')");
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('expense-transport', 'Transportation', 'Gas, public transport, car payments', '#2196F3', 'EXPENSE')");
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('expense-housing', 'Housing', 'Rent, mortgage, utilities, maintenance', '#9C27B0', 'EXPENSE')");
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('expense-entertainment', 'Entertainment', 'Movies, games, hobbies', '#E91E63', 'EXPENSE')");
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('expense-healthcare', 'Healthcare', 'Medical, dental, pharmacy', '#F44336', 'EXPENSE')");
        stmt.execute("INSERT OR IGNORE INTO categories (id, name, description, color, type) VALUES " +
                "('expense-shopping', 'Shopping', 'Clothing, electronics, misc purchases', '#607D8B', 'EXPENSE')");
    }
}