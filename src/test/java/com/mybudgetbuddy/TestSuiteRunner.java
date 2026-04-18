package com.mybudgetbuddy;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test Suite Runner for MyBudgetBuddy Application
 * 
 * Executes all test cases covering the 7 main test scenarios:
 * TC-001: Valid transaction entry and submission
 * TC-002: Budget creation with category and limit
 * TC-003: Multiple transactions and total calculation
 * TC-004: Financial report generation
 * TC-005: Budget limit exceeded alert
 * TC-006: Invalid transaction data validation
 * TC-007: Transaction deletion
 * 
 * Usage: Run this class to execute all tests
 */
@Suite
@SuiteDisplayName("MyBudgetBuddy Comprehensive Test Suite")
@SelectPackages({
    "com.mybudgetbuddy",
    "com.mybudgetbuddy.validation", 
    "com.mybudgetbuddy.reports",
    "com.mybudgetbuddy.budget"
})
public class TestSuiteRunner {
    
    /**
     * Test Coverage Summary:
     * 
     * 1. ComprehensiveSystemTestSuite.java
     *    - All 7 main test cases (TC-001 through TC-007)
     *    - Integration testing across services
     *    - End-to-end workflow validation
     * 
     * 2. ValidationTestSuite.java
     *    - Detailed TC-006 validation scenarios
     *    - Input sanitization and security
     *    - Business rules validation
     *    - Data type and format validation
     * 
     * 3. ReportGenerationTestSuite.java
     *    - Comprehensive TC-004 report testing
     *    - Chart data preparation
     *    - Performance testing
     *    - Report export validation
     * 
     * 4. BudgetManagementTestSuite.java
     *    - Detailed TC-002 and TC-005 budget scenarios
     *    - Alert generation and management
     *    - Period management and resets
     *    - Integration with transaction data
     * 
     * 5. BudgetServiceTest.java (Updated)
     *    - Legacy test updated for current architecture
     *    - Basic transaction service functionality
     *    - CRUD operations validation
     */
}