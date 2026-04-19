package com.mybudgetbuddy.validation;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.domain.model.Budget;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/**
 * Focused validation test cases for business rules and data validation.
 * Covers TC-006 in detail and extends validation testing across all entities.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ValidationTests {

    @Test
    @Order(1)
    @DisplayName("Transaction Validation: Amount Rules")
    void testTransactionAmountValidation() {
        // Test negative amounts
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionAmount(new BigDecimal("-100.00"));
        }, "Negative amounts should be rejected");

        // Test zero amounts
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionAmount(BigDecimal.ZERO);
        }, "Zero amounts should be rejected");

        // Test valid positive amounts
        assertDoesNotThrow(() -> {
            validateTransactionAmount(new BigDecimal("0.01"));
            validateTransactionAmount(new BigDecimal("100.00"));
            validateTransactionAmount(new BigDecimal("9999999.99"));
        }, "Valid positive amounts should be accepted");

        // Test excessive precision (more than 2 decimal places)
        BigDecimal excessivePrecision = new BigDecimal("100.123");
        assertEquals(2, excessivePrecision.scale(), 
            "Amount should be limited to 2 decimal places");
    }

    @Test
    @Order(2)
    @DisplayName("Transaction Validation: Required Fields")
    void testTransactionRequiredFields() {
        // Test null description
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionDescription(null);
        }, "Null description should be rejected");

        // Test empty description
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionDescription("");
        }, "Empty description should be rejected");

        // Test whitespace-only description
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionDescription("   ");
        }, "Whitespace-only description should be rejected");

        // Test valid description
        assertDoesNotThrow(() -> {
            validateTransactionDescription("Valid Transaction Description");
        }, "Valid description should be accepted");

        // Test description length limits
        String longDescription = "A".repeat(256); // Assuming 255 char limit
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionDescription(longDescription);
        }, "Overly long descriptions should be rejected");
    }

    @Test
    @Order(3)
    @DisplayName("Transaction Validation: Date Rules")
    void testTransactionDateValidation() {
        LocalDate today = LocalDate.now();

        // Test past dates (should be valid)
        assertDoesNotThrow(() -> {
            validateTransactionDate(today.minusDays(1));
            validateTransactionDate(today.minusMonths(6));
            validateTransactionDate(today.minusYears(1));
        }, "Past dates should be valid");

        // Test today (should be valid)
        assertDoesNotThrow(() -> {
            validateTransactionDate(today);
        }, "Today's date should be valid");

        // Test future dates within reasonable range (might be valid for planned transactions)
        assertDoesNotThrow(() -> {
            validateTransactionDate(today.plusDays(7));
        }, "Near future dates should be valid for planned transactions");

        // Test far future dates (business rule: reject dates more than 1 year in future)
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionDate(today.plusYears(2));
        }, "Far future dates should be rejected");

        // Test very old dates (business rule: reject dates more than 10 years old)
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionDate(today.minusYears(11));
        }, "Very old dates should be rejected");
    }

    @Test
    @Order(4)
    @DisplayName("Transaction Validation: Category Rules")
    void testTransactionCategoryValidation() {
        // Test null category ID
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionCategoryId(null);
        }, "Null category ID should be rejected");

        // Test empty category ID
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionCategoryId("");
        }, "Empty category ID should be rejected");

        // Test invalid category format
        assertThrows(IllegalArgumentException.class, () -> {
            validateTransactionCategoryId("invalid-category-format");
        }, "Invalid category format should be rejected");

        // Test valid category IDs
        assertDoesNotThrow(() -> {
            validateTransactionCategoryId("expense-food");
            validateTransactionCategoryId("income-salary");
            validateTransactionCategoryId("expense-transport");
        }, "Valid category IDs should be accepted");
    }

    @Test
    @Order(5)
    @DisplayName("Budget Validation: Amount and Period Rules")
    void testBudgetValidation() {
        // Test negative budget amount
        Budget budget = new Budget();
        assertThrows(IllegalArgumentException.class, () -> {
            budget.setAllocatedAmount(new BigDecimal("-500.00"));
            validateBudget(budget);
        }, "Negative budget amounts should be rejected");

        // Test zero budget amount
        assertThrows(IllegalArgumentException.class, () -> {
            budget.setAllocatedAmount(BigDecimal.ZERO);
            validateBudget(budget);
        }, "Zero budget amounts should be rejected");

        // Test valid budget amounts
        assertDoesNotThrow(() -> {
            budget.setAllocatedAmount(new BigDecimal("500.00"));
            budget.setCategoryId("expense-food");
            budget.setName("Food Budget");
            budget.setBudgetPeriod(Period.ofMonths(1));
            validateBudget(budget);
        }, "Valid budget should be accepted");

        // Test budget period validation
        assertThrows(IllegalArgumentException.class, () -> {
            budget.setBudgetPeriod(null);
            validateBudget(budget);
        }, "Null budget period should be rejected");

        // Test budget threshold validation
        assertThrows(IllegalArgumentException.class, () -> {
            budget.setWarningThreshold(new BigDecimal("1.5")); // 150% - invalid
            validateBudgetThreshold(budget);
        }, "Warning threshold above 100% should be rejected");

        assertThrows(IllegalArgumentException.class, () -> {
            budget.setWarningThreshold(new BigDecimal("-0.1")); // Negative - invalid
            validateBudgetThreshold(budget);
        }, "Negative warning threshold should be rejected");

        assertDoesNotThrow(() -> {
            budget.setWarningThreshold(new BigDecimal("0.8")); // 80% - valid
            validateBudgetThreshold(budget);
        }, "Valid warning threshold should be accepted");
    }

    @Test
    @Order(6)
    @DisplayName("Input Sanitization: XSS and SQL Injection Prevention")
    void testInputSanitization() {
        // Test XSS prevention in transaction descriptions
        String xssAttempt = "<script>alert('xss')</script>";
        String sanitizedXSS = sanitizeInput(xssAttempt);
        assertFalse(sanitizedXSS.contains("<script>"), 
            "XSS scripts should be removed from input");

        // Test SQL injection prevention
        String sqlInjection = "'; DROP TABLE transactions; --";
        String sanitizedSQL = sanitizeInput(sqlInjection);
        assertFalse(sanitizedSQL.contains("DROP TABLE"), 
            "SQL injection attempts should be sanitized");

        // Test normal input preservation
        String normalInput = "Lunch at Joe's Restaurant & Bar";
        String sanitizedNormal = sanitizeInput(normalInput);
        assertTrue(sanitizedNormal.contains("Lunch") && sanitizedNormal.contains("Restaurant"), 
            "Normal input should be preserved");

        // Test special characters handling
        String specialChars = "Transaction with special chars: @#$%^&*()";
        assertDoesNotThrow(() -> {
            sanitizeInput(specialChars);
        }, "Special characters should be handled gracefully");
    }

    @Test
    @Order(7)
    @DisplayName("Business Rules Validation")
    void testBusinessRulesValidation() {
        // Test income transaction validation
        Transaction incomeTransaction = new Transaction();
        incomeTransaction.setType(TransactionType.INCOME);
        incomeTransaction.setAmount(new BigDecimal("1000.00"));
        
        // Income transactions should not have certain expense-only categories
        assertThrows(IllegalArgumentException.class, () -> {
            incomeTransaction.setCategoryId("expense-food");
            validateTransactionBusinessRules(incomeTransaction);
        }, "Income transactions should not use expense categories");

        // Test expense transaction validation
        Transaction expenseTransaction = new Transaction();
        expenseTransaction.setType(TransactionType.EXPENSE);
        expenseTransaction.setAmount(new BigDecimal("100.00"));
        
        // Expense transactions should not have income-only categories
        assertThrows(IllegalArgumentException.class, () -> {
            expenseTransaction.setCategoryId("income-salary");
            validateTransactionBusinessRules(expenseTransaction);
        }, "Expense transactions should not use income categories");

        // Test daily transaction limit (business rule example)
        BigDecimal largeAmount = new BigDecimal("50000.00");
        assertThrows(IllegalArgumentException.class, () -> {
            validateDailyTransactionLimit(largeAmount);
        }, "Transactions exceeding daily limit should be rejected");
    }

    @Test
    @Order(8)
    @DisplayName("Data Type and Format Validation")
    void testDataTypeAndFormatValidation() {
        // Test UUID format validation
        assertThrows(IllegalArgumentException.class, () -> {
            validateUUIDFormat("invalid-uuid");
        }, "Invalid UUID format should be rejected");

        assertDoesNotThrow(() -> {
            validateUUIDFormat("12345678-1234-1234-1234-123456789abc");
        }, "Valid UUID format should be accepted");

        // Test currency format validation
        assertThrows(IllegalArgumentException.class, () -> {
            validateCurrencyFormat("$100.00"); // Contains currency symbol
        }, "Currency with symbols should be rejected");

        assertThrows(IllegalArgumentException.class, () -> {
            validateCurrencyFormat("100.123"); // Too many decimal places
        }, "Currency with too many decimals should be rejected");

        assertDoesNotThrow(() -> {
            validateCurrencyFormat("100.00");
            validateCurrencyFormat("1234567.89");
        }, "Valid currency formats should be accepted");
    }

    // Validation helper methods (these would typically be in a ValidationService)
    
    private void validateTransactionAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount cannot have more than 2 decimal places");
        }
    }

    private void validateTransactionDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (description.length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }
    }

    private void validateTransactionDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
        
        LocalDate today = LocalDate.now();
        if (date.isAfter(today.plusYears(1))) {
            throw new IllegalArgumentException("Transaction date cannot be more than 1 year in the future");
        }
        if (date.isBefore(today.minusYears(10))) {
            throw new IllegalArgumentException("Transaction date cannot be more than 10 years in the past");
        }
    }

    private void validateTransactionCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
        
        // Basic category ID format validation (expense-xxx or income-xxx)
        if (!categoryId.matches("^(expense|income)-[a-z]+$")) {
            throw new IllegalArgumentException("Invalid category ID format");
        }
    }

    private void validateBudget(Budget budget) {
        if (budget.getAllocatedAmount() == null || budget.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget amount must be positive");
        }
        if (budget.getCategoryId() == null || budget.getCategoryId().trim().isEmpty()) {
            throw new IllegalArgumentException("Budget must have a category");
        }
        if (budget.getName() == null || budget.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Budget must have a name");
        }
        if (budget.getBudgetPeriod() == null) {
            throw new IllegalArgumentException("Budget must have a period");
        }
    }

    private void validateBudgetThreshold(Budget budget) {
        BigDecimal threshold = budget.getWarningThreshold();
        if (threshold == null) return; // Optional field
        
        if (threshold.compareTo(BigDecimal.ZERO) < 0 || threshold.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Warning threshold must be between 0 and 1");
        }
    }

    private String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove potential XSS scripting
        String sanitized = input.replaceAll("<script[^>]*>.*?</script>", "");
        
        // Remove potential SQL injection keywords (basic example)
        sanitized = sanitized.replaceAll("(?i)(DROP\\s+TABLE|DELETE\\s+FROM|INSERT\\s+INTO)", "");
        
        return sanitized.trim();
    }

    private void validateTransactionBusinessRules(Transaction transaction) {
        if (transaction.getType() == TransactionType.INCOME && 
            transaction.getCategoryId() != null && 
            transaction.getCategoryId().startsWith("expense-")) {
            throw new IllegalArgumentException("Income transactions cannot use expense categories");
        }
        
        if (transaction.getType() == TransactionType.EXPENSE && 
            transaction.getCategoryId() != null && 
            transaction.getCategoryId().startsWith("income-")) {
            throw new IllegalArgumentException("Expense transactions cannot use income categories");
        }
    }

    private void validateDailyTransactionLimit(BigDecimal amount) {
        BigDecimal dailyLimit = new BigDecimal("10000.00"); // $10,000 daily limit example
        if (amount.compareTo(dailyLimit) > 0) {
            throw new IllegalArgumentException("Transaction exceeds daily limit of $" + dailyLimit);
        }
    }

    private void validateUUIDFormat(String uuid) {
        if (uuid == null || !uuid.matches("^[0-9a-fA-F-]{36}$")) {
            throw new IllegalArgumentException("Invalid UUID format");
        }
    }

    private void validateCurrencyFormat(String amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        
        // Should not contain currency symbols
        if (amount.contains("$") || amount.contains("€") || amount.contains("£")) {
            throw new IllegalArgumentException("Amount should not contain currency symbols");
        }
        
        // Should match currency decimal pattern (max 2 decimal places)
        if (!amount.matches("^\\d+(\\.\\d{1,2})?$")) {
            throw new IllegalArgumentException("Invalid currency format");
        }
    }
}