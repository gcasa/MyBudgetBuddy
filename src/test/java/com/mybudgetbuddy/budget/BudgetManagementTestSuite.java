package com.mybudgetbuddy.budget;

import com.mybudgetbuddy.domain.model.Budget;
import com.mybudgetbuddy.domain.model.BudgetType;
import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.application.service.impl.TransactionServiceImpl;
import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Comprehensive tests for budget functionality covering:
 * TC-002: Budget creation with category and limit
 * TC-005: Budget limit exceeded alert
 * Additional budget monitoring and management scenarios
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetManagementTests {

    private TransactionService transactionService;
    private List<Budget> testBudgets;
    private String testPlanId;
    private String testCategoryId;

    @BeforeEach
    void setUp() {
        // Use test-specific data directory
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
        
        transactionService = new TransactionServiceImpl();
        testBudgets = new ArrayList<>();
        testPlanId = UUID.randomUUID().toString();
        testCategoryId = "expense-food";
        // Clean up transactions for isolation
        transactionService.getAllTransactions().forEach(t -> transactionService.deleteTransaction(t.getId()));
    }

    @AfterEach
    void tearDown() {
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
    }

    // TC-002: Create a budget with defined category and limit
    @Test
    @Order(1)
    @DisplayName("TC-002.1: Basic Budget Creation with Category and Limit")
    void testBasicBudgetCreationWithCategoryAndLimit() {
        // Arrange & Act
        Budget monthlyFoodBudget = new Budget();
        monthlyFoodBudget.setName("Monthly Food Budget");
        monthlyFoodBudget.setCategoryId(testCategoryId);
        monthlyFoodBudget.setAllocatedAmount(new BigDecimal("500.00"));
        monthlyFoodBudget.setBudgetPeriod(Period.ofMonths(1));
        monthlyFoodBudget.setStartDate(LocalDate.now().withDayOfMonth(1));
        monthlyFoodBudget.setEndDate(LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1));
        monthlyFoodBudget.setPlanId(testPlanId);
        monthlyFoodBudget.setType(BudgetType.MONTHLY_BUDGET);
        monthlyFoodBudget.setWarningThreshold(new BigDecimal("0.8")); // 80% warning threshold

        // Assert - Budget is stored with correct properties
        assertEquals("Monthly Food Budget", monthlyFoodBudget.getName());
        assertEquals(testCategoryId, monthlyFoodBudget.getCategoryId());
        assertEquals(new BigDecimal("500.00"), monthlyFoodBudget.getAllocatedAmount());
        assertEquals(Period.ofMonths(1), monthlyFoodBudget.getBudgetPeriod());
        assertEquals(BudgetType.MONTHLY_BUDGET, monthlyFoodBudget.getType());
        assertTrue(monthlyFoodBudget.isActive());

        // Assert - Budget calculations are correct initially
        assertEquals(new BigDecimal("500.00"), monthlyFoodBudget.getRemainingAmount());
        assertEquals(new BigDecimal("0.00"), monthlyFoodBudget.getUsagePercentage());
        assertEquals(BigDecimal.ZERO, monthlyFoodBudget.getSpentAmount());
        assertFalse(monthlyFoodBudget.isOverBudget());
        assertFalse(monthlyFoodBudget.isNearWarningThreshold());

        testBudgets.add(monthlyFoodBudget);

        System.out.println("✓ Budget created successfully:");
        System.out.println("  Name: " + monthlyFoodBudget.getName());
        System.out.println("  Category: " + monthlyFoodBudget.getCategoryId());
        System.out.println("  Allocated Amount: $" + monthlyFoodBudget.getAllocatedAmount());
        System.out.println("  Period: " + monthlyFoodBudget.getBudgetPeriod());
        System.out.println("  Warning Threshold: " + monthlyFoodBudget.getWarningThreshold().multiply(new BigDecimal("100")) + "%");
    }

    @Test
    @Order(2)
    @DisplayName("TC-002.2: Multiple Budget Categories Creation")
    void testMultipleBudgetCategoriesCreation() {
        // Create budgets for different categories
        Budget foodBudget = createBudget("Food Budget", "expense-food", "600.00", BudgetType.MONTHLY_BUDGET);
        Budget transportBudget = createBudget("Transport Budget", "expense-transport", "300.00", BudgetType.MONTHLY_BUDGET);
        Budget entertainmentBudget = createBudget("Entertainment Budget", "expense-entertainment", "200.00", BudgetType.MONTHLY_BUDGET);
        Budget housingBudget = createBudget("Housing Budget", "expense-housing", "1500.00", BudgetType.MONTHLY_BUDGET);

        // Assert all budgets are created correctly
        assertEquals("expense-food", foodBudget.getCategoryId());
        assertEquals(new BigDecimal("600.00"), foodBudget.getAllocatedAmount());
        
        assertEquals("expense-transport", transportBudget.getCategoryId());
        assertEquals(new BigDecimal("300.00"), transportBudget.getAllocatedAmount());
        
        assertEquals("expense-entertainment", entertainmentBudget.getCategoryId());
        assertEquals(new BigDecimal("200.00"), entertainmentBudget.getAllocatedAmount());
        
        assertEquals("expense-housing", housingBudget.getCategoryId());
        assertEquals(new BigDecimal("1500.00"), housingBudget.getAllocatedAmount());

        // Calculate total budget allocation
        BigDecimal totalBudgetAllocation = testBudgets.stream()
            .map(Budget::getAllocatedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("2600.00"), totalBudgetAllocation);

        System.out.println("✓ Multiple budgets created:");
        System.out.println("  Total Categories: " + testBudgets.size());
        System.out.println("  Total Allocation: $" + totalBudgetAllocation);
        testBudgets.forEach(b -> System.out.println("    " + b.getName() + ": $" + b.getAllocatedAmount()));
    }

    @Test
    @Order(3)
    @DisplayName("TC-002.3: Budget Visibility in Dashboard")
    void testBudgetVisibilityInDashboard() {
        // Create a budget
        Budget dashboardBudget = createBudget("Dashboard Test Budget", testCategoryId, "1000.00", BudgetType.MONTHLY_BUDGET);
        
        // Simulate spending to show dashboard data
        dashboardBudget.setSpentAmount(new BigDecimal("350.00"));

        // Assert budget is visible with correct dashboard metrics
        assertTrue(dashboardBudget.isActive(), "Budget should be active and visible");
        
        // Dashboard metrics calculations
        BigDecimal remainingAmount = dashboardBudget.getRemainingAmount();
        BigDecimal usagePercentage = dashboardBudget.getUsagePercentage();
        boolean nearThreshold = dashboardBudget.isNearWarningThreshold();
        boolean overBudget = dashboardBudget.isOverBudget();

        assertEquals(new BigDecimal("650.00"), remainingAmount, "Remaining amount should be visible");
        assertEquals(new BigDecimal("35.00"), usagePercentage, "Usage percentage should be calculated correctly");
        assertFalse(nearThreshold, "Should not be near threshold at 35% usage");
        assertFalse(overBudget, "Should not be over budget");

        // Test dashboard data formatting
        String formattedRemaining = String.format("$%.2f", remainingAmount.doubleValue());
        String formattedUsage = String.format("%.1f%%", usagePercentage.doubleValue());
        
        assertEquals("$650.00", formattedRemaining, "Dashboard should format remaining amount correctly");
        assertEquals("35.0%", formattedUsage, "Dashboard should format usage percentage correctly");

        System.out.println("✓ Budget dashboard visibility:");
        System.out.println("  Budget Name: " + dashboardBudget.getName());
        System.out.println("  Allocated: $" + dashboardBudget.getAllocatedAmount());
        System.out.println("  Spent: $" + dashboardBudget.getSpentAmount());
        System.out.println("  Remaining: " + formattedRemaining);
        System.out.println("  Usage: " + formattedUsage);
    }

    // TC-005: Exceed predefined budget limit
    @Test
    @Order(4)
    @DisplayName("TC-005.1: Budget Limit Exceeded Alert Generation")
    void testBudgetLimitExceededAlertGeneration() {
        // Arrange
        Budget alertBudget = createBudget("Alert Test Budget", testCategoryId, "300.00", BudgetType.MONTHLY_BUDGET);
        alertBudget.setWarningThreshold(new BigDecimal("0.85")); // 85% warning threshold
        
        // Act - Simulate spending that exceeds the budget
        alertBudget.setSpentAmount(new BigDecimal("350.00")); // $50 over budget

        // Assert - System generates alert notification
        assertTrue(alertBudget.isOverBudget(), "Budget should be flagged as over limit");
        
        BigDecimal overAmount = alertBudget.getSpentAmount().subtract(alertBudget.getAllocatedAmount());
        assertEquals(new BigDecimal("50.00"), overAmount, "Should be $50 over budget");
        
        BigDecimal usagePercentage = alertBudget.getUsagePercentage();
        assertEquals(new BigDecimal("116.67"), usagePercentage.setScale(2, RoundingMode.HALF_UP), 
            "Usage percentage should be 116.67%");

        // Generate alert message
        String alertMessage = generateBudgetAlert(alertBudget);
        assertTrue(alertMessage.contains("OVER BUDGET"), "Alert should indicate over budget status");
        assertTrue(alertMessage.contains("$50.00"), "Alert should show over amount");
        assertTrue(alertMessage.contains(alertBudget.getName()), "Alert should include budget name");

        System.out.println("✓ Budget limit exceeded alert:");
        System.out.println("  " + alertMessage);
        System.out.println("  Over Amount: $" + overAmount);
        System.out.println("  Usage: " + usagePercentage.setScale(1, RoundingMode.HALF_UP) + "%");
    }

    @Test
    @Order(5)
    @DisplayName("TC-005.2: Budget Warning Threshold Alert")
    void testBudgetWarningThresholdAlert() {
        // Arrange
        Budget warningBudget = createBudget("Warning Test Budget", testCategoryId, "500.00", BudgetType.MONTHLY_BUDGET);
        warningBudget.setWarningThreshold(new BigDecimal("0.80")); // 80% warning threshold
        
        // Test spending at exactly the warning threshold
        warningBudget.setSpentAmount(new BigDecimal("400.00")); // Exactly 80%
        assertTrue(warningBudget.isNearWarningThreshold(), "Should trigger warning at exactly 80%");
        assertFalse(warningBudget.isOverBudget(), "Should not be over budget yet");

        // Test spending slightly above warning threshold
        warningBudget.setSpentAmount(new BigDecimal("425.00")); // 85%
        assertTrue(warningBudget.isNearWarningThreshold(), "Should still trigger warning at 85%");
        assertFalse(warningBudget.isOverBudget(), "Should not be over budget at 85%");

        // Test spending below warning threshold
        warningBudget.setSpentAmount(new BigDecimal("350.00")); // 70%
        assertFalse(warningBudget.isNearWarningThreshold(), "Should not trigger warning at 70%");

        // Generate warning message
        warningBudget.setSpentAmount(new BigDecimal("425.00")); // Reset to 85% for warning
        String warningMessage = generateBudgetWarning(warningBudget);
        assertTrue(warningMessage.contains("WARNING"), "Warning should indicate warning status");
        assertTrue(warningMessage.contains("85"), "Warning should show usage percentage");

        System.out.println("✓ Budget warning threshold alert:");
        System.out.println("  " + warningMessage);
        System.out.println("  Warning Threshold: " + warningBudget.getWarningThreshold().multiply(new BigDecimal("100")) + "%");
        System.out.println("  Current Usage: " + warningBudget.getUsagePercentage() + "%");
    }

    @Test
    @Order(6)
    @DisplayName("TC-005.3: Multiple Budget Alerts Management")
    void testMultipleBudgetAlertsManagement() {
        // Create multiple budgets with different spending scenarios
        Budget budget1 = createBudget("Food Budget", "expense-food", "400.00", BudgetType.MONTHLY_BUDGET);
        budget1.setSpentAmount(new BigDecimal("450.00")); // Over budget

        Budget budget2 = createBudget("Transport Budget", "expense-transport", "200.00", BudgetType.MONTHLY_BUDGET);
        budget2.setSpentAmount(new BigDecimal("170.00")); // Near warning threshold (85%)

        Budget budget3 = createBudget("Entertainment Budget", "expense-entertainment", "150.00", BudgetType.MONTHLY_BUDGET);
        budget3.setSpentAmount(new BigDecimal("75.00")); // Within normal range (50%)

        List<Budget> allBudgets = List.of(budget1, budget2, budget3);

        // Generate comprehensive alert summary
        List<String> alerts = generateAllBudgetAlerts(allBudgets);
        
        assertEquals(2, alerts.size(), "Should have 2 alerts (1 over budget, 1 warning)");
        
        boolean hasOverBudgetAlert = alerts.stream().anyMatch(alert -> alert.contains("OVER BUDGET"));
        boolean hasWarningAlert = alerts.stream().anyMatch(alert -> alert.contains("WARNING"));
        
        assertTrue(hasOverBudgetAlert, "Should have over budget alert");
        assertTrue(hasWarningAlert, "Should have warning alert");

        // Test alert prioritization (over budget alerts should come first)
        String firstAlert = alerts.get(0);
        assertTrue(firstAlert.contains("OVER BUDGET"), "Over budget alert should be prioritized");

        System.out.println("✓ Multiple budget alerts management:");
        alerts.forEach(alert -> System.out.println("  " + alert));
    }

    @Test
    @Order(7)
    @DisplayName("TC-005.4: Budget Alert Integration with Transactions")
    void testBudgetAlertIntegrationWithTransactions() {
        // Create budget
        Budget integrationBudget = createBudget("Integration Test Budget", testCategoryId, "250.00", BudgetType.MONTHLY_BUDGET);
        
        // Create transactions that will exceed the budget
        createTestTransaction("Restaurant 1", "100.00", testCategoryId);
        createTestTransaction("Groceries", "80.00", testCategoryId);
        createTestTransaction("Coffee Shop", "25.00", testCategoryId);
        createTestTransaction("Restaurant 2", "65.00", testCategoryId); // This will exceed the $250 budget
        
        // Calculate total spending for the category
        BigDecimal totalSpending = transactionService.getCategorySpending(
            LocalDate.now().withDayOfMonth(1), 
            LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        ).getOrDefault(testCategoryId, BigDecimal.ZERO);
        
        // Update budget with actual spending
        integrationBudget.setSpentAmount(totalSpending);
        
        // Assert the budget is now over the limit due to real transactions
        assertTrue(integrationBudget.isOverBudget(), "Budget should be over limit after transactions");
        assertEquals(new BigDecimal("270.00"), totalSpending, "Total spending should be $270.00");
        
        BigDecimal overAmount = integrationBudget.getSpentAmount().subtract(integrationBudget.getAllocatedAmount());
        assertEquals(new BigDecimal("20.00"), overAmount, "Should be $20 over budget");

        System.out.println("✓ Budget alert integration with transactions:");
        System.out.println("  Total Transactions: " + transactionService.getTransactionsByCategory(testCategoryId).size());
        System.out.println("  Total Spending: $" + totalSpending);
        System.out.println("  Budget Limit: $" + integrationBudget.getAllocatedAmount());
        System.out.println("  Over Amount: $" + overAmount);
    }

    @Test
    @Order(8)
    @DisplayName("TC-005.5: Budget Reset and Period Management")
    void testBudgetResetAndPeriodManagement() {
        // Create budget for current month
        Budget periodicBudget = createBudget("Monthly Reset Budget", testCategoryId, "500.00", BudgetType.MONTHLY_BUDGET);
        periodicBudget.setSpentAmount(new BigDecimal("400.00"));
        
        // Verify current state
        assertTrue(periodicBudget.isNearWarningThreshold(), "Should be near warning threshold");
        assertEquals(new BigDecimal("400.00"), periodicBudget.getSpentAmount());
        
        // Simulate month-end reset
        resetBudgetForNewPeriod(periodicBudget);
        
        // Assert budget is reset for new period
        assertEquals(BigDecimal.ZERO, periodicBudget.getSpentAmount(), "Spent amount should be reset to zero");
        assertEquals(new BigDecimal("500.00"), periodicBudget.getRemainingAmount(), "Remaining amount should be full allocation");
        assertFalse(periodicBudget.isOverBudget(), "Should not be over budget after reset");
        assertFalse(periodicBudget.isNearWarningThreshold(), "Should not be near threshold after reset");
        
        // Verify new period dates
        LocalDate newStartDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        LocalDate newEndDate = newStartDate.withDayOfMonth(newStartDate.lengthOfMonth());
        assertEquals(newStartDate, periodicBudget.getStartDate(), "Start date should be updated");
        assertEquals(newEndDate, periodicBudget.getEndDate(), "End date should be updated");

        System.out.println("✓ Budget period reset:");
        System.out.println("  New Period: " + periodicBudget.getStartDate() + " to " + periodicBudget.getEndDate());
        System.out.println("  Reset Spent Amount: $" + periodicBudget.getSpentAmount());
        System.out.println("  Available Amount: $" + periodicBudget.getRemainingAmount());
    }

    // Helper methods

    private Budget createBudget(String name, String categoryId, String amount, BudgetType type) {
        Budget budget = new Budget();
        budget.setName(name);
        budget.setCategoryId(categoryId);
        budget.setAllocatedAmount(new BigDecimal(amount));
        budget.setBudgetPeriod(Period.ofMonths(1));
        budget.setStartDate(LocalDate.now().withDayOfMonth(1));
        budget.setEndDate(LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1));
        budget.setPlanId(testPlanId);
        budget.setType(type);
        budget.setWarningThreshold(new BigDecimal("0.8")); // Default 80% warning
        
        testBudgets.add(budget);
        return budget;
    }

    private void createTestTransaction(String description, String amount, String categoryId) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategoryId(categoryId);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setPlanId(testPlanId);
        transactionService.createTransaction(transaction);
    }

    private String generateBudgetAlert(Budget budget) {
        if (budget.isOverBudget()) {
            BigDecimal overAmount = budget.getSpentAmount().subtract(budget.getAllocatedAmount());
            return String.format("ALERT: %s is OVER BUDGET by $%.2f (%.1f%% used)", 
                budget.getName(), overAmount.doubleValue(), budget.getUsagePercentage().doubleValue());
        }
        return "";
    }

    private String generateBudgetWarning(Budget budget) {
        if (budget.isNearWarningThreshold() && !budget.isOverBudget()) {
            return String.format("WARNING: %s is approaching limit - %.1f%% used ($%.2f of $%.2f)", 
                budget.getName(), budget.getUsagePercentage().doubleValue(),
                budget.getSpentAmount().doubleValue(), budget.getAllocatedAmount().doubleValue());
        }
        return "";
    }

    private List<String> generateAllBudgetAlerts(List<Budget> budgets) {
        List<String> alerts = new ArrayList<>();
        
        // Add over-budget alerts first (highest priority)
        budgets.stream()
            .filter(Budget::isOverBudget)
            .forEach(budget -> alerts.add(generateBudgetAlert(budget)));
        
        // Add warning alerts second
        budgets.stream()
            .filter(budget -> budget.isNearWarningThreshold() && !budget.isOverBudget())
            .forEach(budget -> alerts.add(generateBudgetWarning(budget)));
        
        return alerts;
    }

    private void resetBudgetForNewPeriod(Budget budget) {
        budget.setSpentAmount(BigDecimal.ZERO);
        LocalDate newStart = budget.getStartDate().plusMonths(1);
        budget.setStartDate(newStart);
        budget.setEndDate(newStart.withDayOfMonth(newStart.lengthOfMonth()));
    }
}