package com.mybudgetbuddy.test;

import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.application.service.impl.TransactionServiceImpl;
import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simple test to verify SQLite database integration works.
 * Run this manually to test the database functionality.
 */
public class DatabaseIntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("Starting SQLite database integration test...");
        
        try {
            // Initialize the service (this will create the database)
            TransactionService service = new TransactionServiceImpl();
            System.out.println("✓ Database initialized successfully");
            
            // Create a test transaction
            Transaction transaction = new Transaction();
            transaction.setDescription("Test Transaction");
            transaction.setAmount(new BigDecimal("100.00"));
            transaction.setType(TransactionType.EXPENSE);
            transaction.setCategoryId("expense-food");
            transaction.setTransactionDate(LocalDate.now());
            
            // Save the transaction
            Transaction saved = service.createTransaction(transaction);
            System.out.println("✓ Transaction created with ID: " + saved.getId());
            
            // Retrieve all transactions
            var transactions = service.getAllTransactions();
            System.out.println("✓ Found " + transactions.size() + " transactions");
            
            // Test financial calculations
            BigDecimal totalExpenses = service.getTotalExpenses();
            System.out.println("✓ Total expenses: $" + totalExpenses);
            
            // Clean up - delete the test transaction
            service.deleteTransaction(saved.getId());
            System.out.println("✓ Test transaction cleaned up");
            
            System.out.println("Database integration test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}