package com.mybudgetbuddy.reports;

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive tests for financial report generation (TC-004).
 * Tests report accuracy, formatting, data aggregation, and chart data preparation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportGenerationTests {

    private TransactionService transactionService;

    private String testPlanId;

    @BeforeEach
    void setUp() {
        // Use test-specific data directory
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
        
        transactionService = new TransactionServiceImpl();
        testPlanId = UUID.randomUUID().toString();
        
        setupTestTransactions();
    }

    @AfterEach
    void tearDown() {
        File dataFile = new File(System.getProperty("user.home"), ".mybudgetbuddy_data.ser");
        if (dataFile.exists()) dataFile.delete();
    }

    private void setupTestTransactions() {
        // Create a comprehensive set of test transactions for reporting
        LocalDate today = LocalDate.now();
        
        // Current month transactions
        createTransaction("Salary", "5000.00", TransactionType.INCOME, "income-salary", today);
        createTransaction("Freelance Work", "1200.00", TransactionType.INCOME, "income-freelance", today.minusDays(5));
        
        createTransaction("Rent", "1500.00", TransactionType.EXPENSE, "expense-housing", today.minusDays(1));
        createTransaction("Groceries", "300.00", TransactionType.EXPENSE, "expense-food", today.minusDays(3));
        createTransaction("Restaurant", "85.50", TransactionType.EXPENSE, "expense-food", today.minusDays(7));
        createTransaction("Gas", "60.00", TransactionType.EXPENSE, "expense-transport", today.minusDays(2));
        createTransaction("Utilities", "150.00", TransactionType.EXPENSE, "expense-utilities", today.minusDays(4));
        
        // Previous month transactions
        LocalDate lastMonth = today.minusMonths(1);
        createTransaction("Previous Salary", "5000.00", TransactionType.INCOME, "income-salary", lastMonth);
        createTransaction("Previous Rent", "1500.00", TransactionType.EXPENSE, "expense-housing", lastMonth.minusDays(5));
        createTransaction("Previous Groceries", "280.00", TransactionType.EXPENSE, "expense-food", lastMonth.minusDays(10));
    }

    private void createTransaction(String description, String amount, TransactionType type, String categoryId, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setType(type);
        transaction.setCategoryId(categoryId);
        transaction.setTransactionDate(date);
        transaction.setPlanId(testPlanId);
        transactionService.createTransaction(transaction);
    }

    @Test
    @Order(1)
    @DisplayName("TC-004.1: Monthly Financial Summary Report")
    void testMonthlyFinancialSummaryReport() {
        // Arrange
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // Act - Generate monthly report data
        BigDecimal totalIncome = transactionService.getTotalIncomeForPeriod(startOfMonth, endOfMonth);
        BigDecimal totalExpenses = transactionService.getTotalExpensesForPeriod(startOfMonth, endOfMonth);
        BigDecimal netIncome = totalIncome.subtract(totalExpenses);
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startOfMonth, endOfMonth);

        // Assert - Report displays accurate summaries
        assertEquals(new BigDecimal("6200.00"), totalIncome, "Monthly total income should be correct");
        assertEquals(new BigDecimal("2095.50"), totalExpenses, "Monthly total expenses should be correct");
        assertEquals(new BigDecimal("4104.50"), netIncome, "Monthly net income should be correct");
        
        // Verify transaction count for current month
        long currentMonthTransactionCount = transactions.stream()
            .filter(t -> !t.getTransactionDate().isBefore(startOfMonth) && 
                        !t.getTransactionDate().isAfter(endOfMonth))
            .count();
        assertEquals(7, currentMonthTransactionCount, "Should have 7 transactions for current month");

        System.out.println("✓ Monthly Financial Summary:");
        System.out.println("  Total Income: $" + totalIncome);
        System.out.println("  Total Expenses: $" + totalExpenses);
        System.out.println("  Net Income: $" + netIncome);
        System.out.println("  Transactions: " + currentMonthTransactionCount);
    }

    @Test
    @Order(2)
    @DisplayName("TC-004.2: Category Spending Breakdown Report")
    void testCategorySpendingBreakdownReport() {
        // Arrange
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // Act - Generate category spending report
        Map<String, BigDecimal> categorySpending = transactionService.getCategorySpending(startOfMonth, endOfMonth);

        // Assert - Report shows accurate category breakdowns
        assertNotNull(categorySpending, "Category spending map should not be null");
        assertTrue(categorySpending.size() > 0, "Should have spending categories");

        // Verify specific category amounts
        BigDecimal foodSpending = categorySpending.getOrDefault("expense-food", BigDecimal.ZERO);
        assertEquals(new BigDecimal("385.50"), foodSpending, "Food category spending should be correct");

        BigDecimal housingSpending = categorySpending.getOrDefault("expense-housing", BigDecimal.ZERO);
        assertEquals(new BigDecimal("1500.00"), housingSpending, "Housing category spending should be correct");

        BigDecimal transportSpending = categorySpending.getOrDefault("expense-transport", BigDecimal.ZERO);
        assertEquals(new BigDecimal("60.00"), transportSpending, "Transport category spending should be correct");

        // Calculate spending percentages
        BigDecimal totalExpenses = transactionService.getTotalExpensesForPeriod(startOfMonth, endOfMonth);
        
        System.out.println("✓ Category Spending Breakdown:");
        categorySpending.forEach((category, amount) -> {
            BigDecimal percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            System.out.println("  " + category + ": $" + amount + " (" + percentage.setScale(1, RoundingMode.HALF_UP) + "%)");
        });
    }

    @Test
    @Order(3)
    @DisplayName("TC-004.3: Income vs Expenses Trend Report")
    void testIncomeVsExpensesTrendReport() {
        // Arrange - Compare current month vs previous month
        LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate currentMonthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = previousMonthStart.withDayOfMonth(previousMonthStart.lengthOfMonth());

        // Act - Generate trend data
        BigDecimal currentIncome = transactionService.getTotalIncomeForPeriod(currentMonthStart, currentMonthEnd);
        BigDecimal currentExpenses = transactionService.getTotalExpensesForPeriod(currentMonthStart, currentMonthEnd);
        BigDecimal previousIncome = transactionService.getTotalIncomeForPeriod(previousMonthStart, previousMonthEnd);
        BigDecimal previousExpenses = transactionService.getTotalExpensesForPeriod(previousMonthStart, previousMonthEnd);

        // Assert - Trend calculations are correct
        assertEquals(new BigDecimal("6200.00"), currentIncome);
        assertEquals(new BigDecimal("2095.50"), currentExpenses);
        assertEquals(new BigDecimal("5000.00"), previousIncome);
        assertEquals(new BigDecimal("1780.00"), previousExpenses);

        // Calculate growth rates
        BigDecimal incomeGrowth = currentIncome.subtract(previousIncome).divide(previousIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        BigDecimal expenseGrowth = currentExpenses.subtract(previousExpenses).divide(previousExpenses, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

        System.out.println("✓ Income vs Expenses Trend:");
        System.out.println("  Current Month Income: $" + currentIncome);
        System.out.println("  Previous Month Income: $" + previousIncome);
        System.out.println("  Income Growth: " + incomeGrowth.setScale(1, RoundingMode.HALF_UP) + "%");
        System.out.println("  Current Month Expenses: $" + currentExpenses);
        System.out.println("  Previous Month Expenses: $" + previousExpenses);
        System.out.println("  Expense Growth: " + expenseGrowth.setScale(1, RoundingMode.HALF_UP) + "%");
    }

    @Test
    @Order(4)
    @DisplayName("TC-004.4: Transaction List Report with Filtering")
    void testTransactionListReportWithFiltering() {
        // Test filtering by transaction type
        List<Transaction> incomeTransactions = transactionService.getTransactionsByType(TransactionType.INCOME);
        List<Transaction> expenseTransactions = transactionService.getTransactionsByType(TransactionType.EXPENSE);

        assertTrue(incomeTransactions.size() > 0, "Should have income transactions");
        assertTrue(expenseTransactions.size() > 0, "Should have expense transactions");

        // Verify all income transactions are income type
        boolean allIncomeTransactionsValid = incomeTransactions.stream()
            .allMatch(t -> t.getType() == TransactionType.INCOME);
        assertTrue(allIncomeTransactionsValid, "All filtered transactions should be income type");

        // Verify all expense transactions are expense type
        boolean allExpenseTransactionsValid = expenseTransactions.stream()
            .allMatch(t -> t.getType() == TransactionType.EXPENSE);
        assertTrue(allExpenseTransactionsValid, "All filtered transactions should be expense type");

        // Test filtering by category
        List<Transaction> foodTransactions = transactionService.getTransactionsByCategory("expense-food");
        assertEquals(2, foodTransactions.size(), "Should have 2 food transactions in current data");

        // Verify all food transactions are in food category
        boolean allFoodTransactionsValid = foodTransactions.stream()
            .allMatch(t -> "expense-food".equals(t.getCategoryId()));
        assertTrue(allFoodTransactionsValid, "All filtered transactions should be food category");

        System.out.println("✓ Transaction Filtering:");
        System.out.println("  Income Transactions: " + incomeTransactions.size());
        System.out.println("  Expense Transactions: " + expenseTransactions.size());
        System.out.println("  Food Category Transactions: " + foodTransactions.size());
    }

    @Test
    @Order(5)
    @DisplayName("TC-004.5: Chart Data Preparation for Visualization")
    void testChartDataPreparationForVisualization() {
        // Arrange
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // Act - Prepare data for different chart types
        
        // 1. Pie chart data (category spending)
        Map<String, BigDecimal> pieChartData = transactionService.getCategorySpending(startOfMonth, endOfMonth);
        assertNotNull(pieChartData, "Pie chart data should not be null");
        assertTrue(pieChartData.size() > 0, "Pie chart should have data points");

        // 2. Bar chart data (monthly comparison) - mock implementation
        Map<String, BigDecimal> barChartData = Map.of(
            "Current Month Income", transactionService.getTotalIncomeForPeriod(startOfMonth, endOfMonth),
            "Current Month Expenses", transactionService.getTotalExpensesForPeriod(startOfMonth, endOfMonth),
            "Previous Month Income", transactionService.getTotalIncomeForPeriod(startOfMonth.minusMonths(1), endOfMonth.minusMonths(1)),
            "Previous Month Expenses", transactionService.getTotalExpensesForPeriod(startOfMonth.minusMonths(1), endOfMonth.minusMonths(1))
        );

        // 3. Line chart data (daily spending trend) - mock implementation
        List<Transaction> dailyTransactions = transactionService.getTransactionsByDateRange(startOfMonth, endOfMonth);
        assertNotNull(dailyTransactions, "Daily transactions should not be null");

        // Assert - Chart data is properly formatted
        assertEquals(4, barChartData.size(), "Bar chart should have 4 data points");
        assertTrue(dailyTransactions.size() > 0, "Line chart should have transaction data");

        // Verify data integrity for charts
        BigDecimal totalPieChartAmount = pieChartData.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = transactionService.getTotalExpensesForPeriod(startOfMonth, endOfMonth);
        assertEquals(totalExpenses, totalPieChartAmount, "Pie chart total should match total expenses");

        System.out.println("✓ Chart Data Preparation:");
        System.out.println("  Pie Chart Categories: " + pieChartData.size());
        System.out.println("  Bar Chart Data Points: " + barChartData.size());
        System.out.println("  Line Chart Transactions: " + dailyTransactions.size());
        System.out.println("  Data Integrity Check: PASSED");
    }

    @Test
    @Order(6)
    @DisplayName("TC-004.6: Report Export and Format Validation")
    void testReportExportAndFormatValidation() {
        // This test would validate report export functionality
        // Since we don't have file export implementation, we'll test the data preparation
        
        // Arrange
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // Act - Prepare report data for export
        ReportData reportData = prepareReportData(startDate, endDate);

        // Assert - Report data is complete and formatted correctly
        assertNotNull(reportData, "Report data should not be null");
        assertNotNull(reportData.summary, "Report summary should not be null");
        assertTrue(reportData.transactions.size() > 0, "Report should include transactions");
        assertTrue(reportData.categoryBreakdown.size() > 0, "Report should include category breakdown");

        // Verify date range formatting
        String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        assertEquals(formattedStartDate, reportData.periodStart, "Start date should be properly formatted");
        assertEquals(formattedEndDate, reportData.periodEnd, "End date should be properly formatted");

        // Verify numerical formatting
        assertTrue(reportData.summary.totalIncome.scale() <= 2, "Income should have max 2 decimal places");
        assertTrue(reportData.summary.totalExpenses.scale() <= 2, "Expenses should have max 2 decimal places");

        System.out.println("✓ Report Export Format Validation:");
        System.out.println("  Period: " + reportData.periodStart + " to " + reportData.periodEnd);
        System.out.println("  Total Income: $" + reportData.summary.totalIncome);
        System.out.println("  Total Expenses: $" + reportData.summary.totalExpenses);
        System.out.println("  Transaction Count: " + reportData.transactions.size());
        System.out.println("  Category Count: " + reportData.categoryBreakdown.size());
    }

    @Test
    @Order(7)
    @DisplayName("TC-004.7: Report Performance and Large Dataset Handling")
    void testReportPerformanceAndLargeDatasetHandling() {
        // Create additional transactions to simulate larger dataset
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 100; i++) {
            createTransaction("Performance Test Transaction " + i, "50.00", 
                TransactionType.EXPENSE, "expense-test", today.minusDays(i % 30));
        }

        // Measure report generation time
        long startTime = System.currentTimeMillis();
        
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        BigDecimal totalIncome = transactionService.getTotalIncomeForPeriod(startOfMonth, endOfMonth);
        BigDecimal totalExpenses = transactionService.getTotalExpensesForPeriod(startOfMonth, endOfMonth);
        Map<String, BigDecimal> categorySpending = transactionService.getCategorySpending(startOfMonth, endOfMonth);
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startOfMonth, endOfMonth);
        
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        // Assert - Report generation completes within reasonable time
        assertTrue(processingTime < 5000, "Report generation should complete within 5 seconds");
        assertTrue(transactions.size() > 100, "Should handle large number of transactions");

        // Verify data accuracy with large dataset
        assertNotNull(totalIncome, "Total income should be calculated");
        assertNotNull(totalExpenses, "Total expenses should be calculated");
        assertTrue(categorySpending.size() > 0, "Category breakdown should work with large dataset");

        System.out.println("✓ Performance Test Results:");
        System.out.println("  Processing Time: " + processingTime + "ms");
        System.out.println("  Total Transactions Processed: " + transactions.size());
        System.out.println("  Categories Analyzed: " + categorySpending.size());
    }

    // Helper method to prepare comprehensive report data
    private ReportData prepareReportData(LocalDate startDate, LocalDate endDate) {
        ReportData data = new ReportData();
        
        data.periodStart = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        data.periodEnd = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        data.summary = new ReportSummary();
        data.summary.totalIncome = transactionService.getTotalIncomeForPeriod(startDate, endDate);
        data.summary.totalExpenses = transactionService.getTotalExpensesForPeriod(startDate, endDate);
        
        data.transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        data.categoryBreakdown = transactionService.getCategorySpending(startDate, endDate);
        
        return data;
    }

    // Helper classes for report data structure
    private static class ReportData {
        String periodStart;
        String periodEnd;
        ReportSummary summary;
        List<Transaction> transactions;
        Map<String, BigDecimal> categoryBreakdown;
    }

    private static class ReportSummary {
        BigDecimal totalIncome;
        BigDecimal totalExpenses;
    }
}