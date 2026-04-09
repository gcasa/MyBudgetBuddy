package com.mybudgetbuddy;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.service.BudgetService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetServiceTest {

    private BudgetService service;

    @BeforeEach
    void setUp() {
        // Use a test-specific data file to avoid touching real user data
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
        service = new BudgetService();
    }

    @AfterEach
    void tearDown() {
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
    }

    @Test
    @Order(1)
    void testAddTransaction() {
        Transaction t = new Transaction("Salary", 3000.0, TransactionType.INCOME, "Income", LocalDate.now());
        service.addTransaction(t);

        List<Transaction> transactions = service.getTransactions();
        assertEquals(1, transactions.size());
        assertEquals("Salary", transactions.get(0).getDescription());
        assertEquals(3000.0, transactions.get(0).getAmount(), 0.001);
        assertEquals(TransactionType.INCOME, transactions.get(0).getType());
    }

    @Test
    @Order(2)
    void testDeleteTransaction() {
        Transaction t1 = new Transaction("Salary", 3000.0, TransactionType.INCOME, "Income", LocalDate.now());
        Transaction t2 = new Transaction("Rent", 1200.0, TransactionType.EXPENSE, "Housing", LocalDate.now());
        service.addTransaction(t1);
        service.addTransaction(t2);
        assertEquals(2, service.getTransactions().size());

        service.deleteTransaction(t1.getId());
        List<Transaction> transactions = service.getTransactions();
        assertEquals(1, transactions.size());
        assertEquals("Rent", transactions.get(0).getDescription());
    }

    @Test
    @Order(3)
    void testGetTotalIncome() {
        service.addTransaction(new Transaction("Salary", 3000.0, TransactionType.INCOME, "Income", LocalDate.now()));
        service.addTransaction(new Transaction("Freelance", 500.0, TransactionType.INCOME, "Income", LocalDate.now()));
        service.addTransaction(new Transaction("Rent", 1200.0, TransactionType.EXPENSE, "Housing", LocalDate.now()));

        assertEquals(3500.0, service.getTotalIncome(), 0.001);
    }

    @Test
    @Order(4)
    void testGetTotalExpenses() {
        service.addTransaction(new Transaction("Salary", 3000.0, TransactionType.INCOME, "Income", LocalDate.now()));
        service.addTransaction(new Transaction("Rent", 1200.0, TransactionType.EXPENSE, "Housing", LocalDate.now()));
        service.addTransaction(new Transaction("Groceries", 200.0, TransactionType.EXPENSE, "Food", LocalDate.now()));

        assertEquals(1400.0, service.getTotalExpenses(), 0.001);
    }

    @Test
    @Order(5)
    void testGetBalance() {
        service.addTransaction(new Transaction("Salary", 3000.0, TransactionType.INCOME, "Income", LocalDate.now()));
        service.addTransaction(new Transaction("Rent", 1200.0, TransactionType.EXPENSE, "Housing", LocalDate.now()));
        service.addTransaction(new Transaction("Groceries", 200.0, TransactionType.EXPENSE, "Food", LocalDate.now()));

        assertEquals(1600.0, service.getBalance(), 0.001);
    }

    @Test
    @Order(6)
    void testUpdateTransaction() {
        Transaction t = new Transaction("Old description", 100.0, TransactionType.EXPENSE, "Food", LocalDate.now());
        service.addTransaction(t);

        t.setDescription("Updated description");
        t.setAmount(150.0);
        service.updateTransaction(t);

        List<Transaction> transactions = service.getTransactions();
        assertEquals(1, transactions.size());
        assertEquals("Updated description", transactions.get(0).getDescription());
        assertEquals(150.0, transactions.get(0).getAmount(), 0.001);
    }

    @Test
    @Order(7)
    void testEmptyServiceHasZeroTotals() {
        assertEquals(0.0, service.getTotalIncome(), 0.001);
        assertEquals(0.0, service.getTotalExpenses(), 0.001);
        assertEquals(0.0, service.getBalance(), 0.001);
    }
}
