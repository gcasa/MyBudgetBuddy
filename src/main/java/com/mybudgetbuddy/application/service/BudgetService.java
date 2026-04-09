package com.mybudgetbuddy.application.service;

import com.mybudgetbuddy.domain.model.Budget;
import com.mybudgetbuddy.domain.model.BudgetType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public interface BudgetService {
    
    // Budget management
    Budget createBudget(Budget budget);
    Budget updateBudget(Budget budget);
    void deleteBudget(String budgetId);
    Optional<Budget> getBudgetById(String budgetId);
    List<Budget> getBudgetsByPlanId(String planId);
    List<Budget> getActiveBudgets(String planId);
    
    // Budget operations
    void setBudget(String budgetId, BigDecimal allocatedAmount);
    void updateBudgetSpending(String budgetId, BigDecimal spentAmount);
    void addSpendingToBudget(String budgetId, BigDecimal additionalSpending);
    void resetBudgetPeriod(String budgetId);
    
    // Budget monitoring
    boolean isOverBudget(String budgetId);
    boolean isNearWarningThreshold(String budgetId);
    BigDecimal getBudgetUsagePercentage(String budgetId);
    BigDecimal getRemainingBudget(String budgetId);
    List<Budget> getOverBudgets(String planId);
    List<Budget> getBudgetsNearThreshold(String planId);
    
    // Budget analysis
    void checkBudgetThresholds(String planId);
    List<String> generateBudgetWarnings(String planId);
    BigDecimal calculateRecommendedBudget(String categoryId, String planId, Period budgetPeriod);
    BigDecimal getHistoricalSpending(String categoryId, String planId, Period period);
    
    // Budget templates
    Budget createBudgetFromTemplate(String templateName, String categoryId, String planId);
    List<String> getAvailableTemplates();
    void saveBudgetAsTemplate(String budgetId, String templateName);
    
    // Rollover and adjustments
    void rolloverUnspentBudget(String budgetId);
    void adjustBudgetForInflation(String budgetId, BigDecimal inflationRate);
    void distributeBudgetAcrossCategories(String planId, BigDecimal totalBudget);
    
    // Bulk operations
    void bulkUpdateBudgets(List<Budget> budgets);
    void copyBudgetsToNewPeriod(String planId, LocalDate newPeriodStart);
    void scaleBudgets(String planId, BigDecimal scaleFactor);
    
    // Reporting
    List<Budget> getBudgetPerformance(String planId, LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalAllocatedBudget(String planId);
    BigDecimal getTotalSpentFromBudgets(String planId);
    BigDecimal getBudgetVariance(String planId);
    
    // Integration with transactions
    void syncBudgetWithTransactions(String budgetId);
    void updateAllBudgetsFromTransactions(String planId);
}