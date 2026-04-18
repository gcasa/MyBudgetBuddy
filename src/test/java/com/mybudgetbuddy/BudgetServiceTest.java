package com.mybudgetbuddy;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.application.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.*;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetServiceTest {

    private TransactionService service;

    @BeforeEach
    void setUp() {
        // Use a test-specific data file to avoid touching real user data
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
        service = new TransactionServiceImpl();
    }

    @AfterEach
    void tearDown() {
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
    }

    @Test
    @Order(1)
    void testAddTransaction() {
        Transaction t = new Transaction();
        t.setDescription("Salary");
        t.setAmount(new BigDecimal("3000.00"));
        t.setType(TransactionType.INCOME);
        t.setCategoryId("income-salary");
        t.setTransactionDate(LocalDate.now());
        
        service.createTransaction(t);

        List<Transaction> transactions = service.getAllTransactions();
        assertEquals(1, transactions.size());
        assertEquals("Salary", transactions.get(0).getDescription());
        assertEquals(new BigDecimal("3000.00"), transactions.get(0).getAmount());
        assertEquals(TransactionType.INCOME, transactions.get(0).getType());
    }

    @Test
    @Order(2)
    void testDeleteTransaction() {
        Transaction t1 = new Transaction();
        t1.setDescription("Salary");
        t1.setAmount(new BigDecimal("3000.00"));
        t1.setType(TransactionType.INCOME);
        t1.setCategoryId("income-salary");
        t1.setTransactionDate(LocalDate.now());
        
        Transaction t2 = new Transaction();
        t2.setDescription("Rent");
        t2.setAmount(new BigDecimal("1200.00"));
        t2.setType(TransactionType.EXPENSE);
        t2.setCategoryId("expense-housing");
        t2.setTransactionDate(LocalDate.now());
        
        Transaction savedT1 = service.createTransaction(t1);
        service.createTransaction(t2);
        assertEquals(2, service.getAllTransactions().size());

        service.deleteTransaction(savedT1.getId());
        List<Transaction> transactions = service.getAllTransactions();
        assertEquals(1, transactions.size());
        assertEquals("Rent", transactions.get(0).getDescription());
    }

    @Test
    @Order(3)
    void testGetTotalIncome() {
        Transaction salary = new Transaction();
        salary.setDescription("Salary");
        salary.setAmount(new BigDecimal("3000.00"));
        salary.setType(TransactionType.INCOME);
        salary.setCategoryId("income-salary");
        salary.setTransactionDate(LocalDate.now());
        
        Transaction freelance = new Transaction();
        freelance.setDescription("Freelance");
        freelance.setAmount(new BigDecimal("500.00"));
        freelance.setType(TransactionType.INCOME);
        freelance.setCategoryId("income-freelance");
        freelance.setTransactionDate(LocalDate.now());
        
        Transaction rent = new Transaction();
        rent.setDescription("Rent");
        rent.setAmount(new BigDecimal("1200.00"));
        rent.setType(TransactionType.EXPENSE);
        rent.setCategoryId("expense-housing");
        rent.setTransactionDate(LocalDate.now());
        
        service.createTransaction(salary);
        service.createTransaction(freelance);
        service.createTransaction(rent);

        assertEquals(new BigDecimal("3500.00"), service.getTotalIncome());
    }

    @Test
    @Order(4)
    void testGetTotalExpenses() {
        Transaction salary = new Transaction();
        salary.setDescription("Salary");
        salary.setAmount(new BigDecimal("3000.00"));
        salary.setType(TransactionType.INCOME);
        salary.setCategoryId("income-salary");
        salary.setTransactionDate(LocalDate.now());
        
        Transaction rent = new Transaction();
        rent.setDescription("Rent");
        rent.setAmount(new BigDecimal("1200.00"));
        rent.setType(TransactionType.EXPENSE);
        rent.setCategoryId("expense-housing");
        rent.setTransactionDate(LocalDate.now());
        
        Transaction groceries = new Transaction();
        groceries.setDescription("Groceries");
        groceries.setAmount(new BigDecimal("200.00"));
        groceries.setType(TransactionType.EXPENSE);
        groceries.setCategoryId("expense-food");
        groceries.setTransactionDate(LocalDate.now());
        
        service.createTransaction(salary);
        service.createTransaction(rent);
        service.createTransaction(groceries);

        assertEquals(new BigDecimal("1400.00"), service.getTotalExpenses());
    }

    @Test
    @Order(5)
    void testGetBalance() {
        Transaction salary = new Transaction();
        salary.setDescription("Salary");
        salary.setAmount(new BigDecimal("3000.00"));
        salary.setType(TransactionType.INCOME);
        salary.setCategoryId("income-salary");
        salary.setTransactionDate(LocalDate.now());
        
        Transaction rent = new Transaction();
        rent.setDescription("Rent");
        rent.setAmount(new BigDecimal("1200.00"));
        rent.setType(TransactionType.EXPENSE);
        rent.setCategoryId("expense-housing");
        rent.setTransactionDate(LocalDate.now());
        
        Transaction groceries = new Transaction();
        groceries.setDescription("Groceries");
        groceries.setAmount(new BigDecimal("200.00"));
        groceries.setType(TransactionType.EXPENSE);
        groceries.setCategoryId("expense-food");
        groceries.setTransactionDate(LocalDate.now());
        
        service.createTransaction(salary);
        service.createTransaction(rent);
        service.createTransaction(groceries);

        assertEquals(new BigDecimal("1600.00"), service.getBalance());
    }

    @Test
    @Order(6)
    void testUpdateTransaction() {
        Transaction t = new Transaction();
        t.setDescription("Old description");
        t.setAmount(new BigDecimal("100.00"));
        t.setType(TransactionType.EXPENSE);
        t.setCategoryId("expense-food");
        t.setTransactionDate(LocalDate.now());
        
        Transaction savedTransaction = service.createTransaction(t);

        savedTransaction.setDescription("Updated description");
        savedTransaction.setAmount(new BigDecimal("150.00"));
        service.updateTransaction(savedTransaction);

        List<Transaction> transactions = service.getAllTransactions();
        assertEquals(1, transactions.size());
        assertEquals("Updated description", transactions.get(0).getDescription());
        assertEquals(new BigDecimal("150.00"), transactions.get(0).getAmount());
    }

    @Test
    @Order(7)
    void testEmptyServiceHasZeroTotals() {
        assertEquals(new BigDecimal("0.00"), service.getTotalIncome());
        assertEquals(new BigDecimal("0.00"), service.getTotalExpenses());
        assertEquals(new BigDecimal("0.00"), service.getBalance());
    }
}
