package com.mybudgetbuddy.engine;

import com.mybudgetbuddy.domain.model.FinancialPlan;
import com.mybudgetbuddy.domain.model.Scenario;
import com.mybudgetbuddy.domain.model.Goal;
import com.mybudgetbuddy.domain.model.Budget;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinancialPlanningEngine {
    
    // Core planning calculations
    FinancialPlan calculateBudgets(FinancialPlan plan);
    Map<String, BigDecimal> runForecasts(String planId, int months);
    Map<String, BigDecimal> projectGoals(String planId, LocalDate targetDate);
    
    // Scenario analysis
    Scenario analyzeScenario(Scenario scenario);
    List<Scenario> compareScenarios(List<String> scenarioIds);
    Scenario runBaselineScenario(String planId);
    
    // Financial projections
    BigDecimal projectFutureValue(BigDecimal presentValue, BigDecimal interestRate, int periods);
    BigDecimal calculateRequiredSavings(BigDecimal goalAmount, BigDecimal currentAmount, 
                                       int monthsToGoal, BigDecimal interestRate);
    BigDecimal projectNetWorth(String planId, int years);
    BigDecimal calculateRetirementNeeds(String planId, int retirementAge, int lifeExpectancy);
    
    // Cash flow analysis
    Map<String, BigDecimal> calculateMonthlyCashFlow(String planId);
    List<BigDecimal> projectCashFlowTrend(String planId, int months);
    BigDecimal calculateEmergencyFundNeeds(String planId);
    boolean isCashFlowPositive(String planId, LocalDate targetDate);
    
    // Budget optimization
    Map<String, BigDecimal> optimizeBudgetAllocation(String planId, BigDecimal totalBudget);
    List<String> identifyBudgetOptimizationOpportunities(String planId);
    BigDecimal calculateOptimalSavingsRate(String planId);
    
    // Goal feasibility analysis
    boolean isGoalFeasible(String goalId, String planId);
    LocalDate calculateGoalCompletionDate(String goalId);
    BigDecimal calculateRequiredMonthlyContribution(String goalId);
    Map<String, BigDecimal> calculateGoalPriorities(List<String> goalIds, String planId);
    
    // Risk analysis
    BigDecimal calculateFinancialRiskScore(String planId);
    List<String> identifyFinancialRisks(String planId);
    BigDecimal calculateVaR(String planId, BigDecimal confidenceLevel); // Value at Risk
    Map<String, BigDecimal> runStressTest(String planId, Map<String, BigDecimal> stressFactors);
    
    // Investment analysis
    BigDecimal calculatePortfolioReturn(Map<String, BigDecimal> allocation, Map<String, BigDecimal> returns);
    BigDecimal calculateOptimalAssetAllocation(String planId, BigDecimal riskTolerance);
    BigDecimal projectInvestmentGrowth(BigDecimal principal, BigDecimal monthlyContribution, 
                                      BigDecimal annualReturn, int years);
    
    // Debt analysis
    BigDecimal calculateDebtPayoffTime(BigDecimal balance, BigDecimal interestRate, BigDecimal monthlyPayment);
    BigDecimal calculateOptimalDebtPayoffStrategy(List<Map<String, BigDecimal>> debts);
    BigDecimal calculateDebtToIncomeRatio(String planId);
    
    // Tax planning
    BigDecimal estimateAnnualTaxLiability(String planId);
    Map<String, BigDecimal> calculateTaxOptimizationOpportunities(String planId);
    BigDecimal calculateAfterTaxIncome(BigDecimal grossIncome, String taxBracket);
    
    // Inflation adjustments
    BigDecimal adjustForInflation(BigDecimal amount, BigDecimal inflationRate, int years);
    Map<String, BigDecimal> projectInflationAdjustedGoals(String planId);
    BigDecimal calculateRealReturnRate(BigDecimal nominalRate, BigDecimal inflationRate);
    
    // Monte Carlo simulations
    List<BigDecimal> runMonteCarloSimulation(String planId, int simulations);
    BigDecimal calculateProbabilityOfSuccess(String goalId, int simulations);
    Map<String, BigDecimal> getSimulationStatistics(List<BigDecimal> results);
    
    // Sensitivity analysis
    Map<String, BigDecimal> runSensitivityAnalysis(String planId, String variable, 
                                                   BigDecimal minValue, BigDecimal maxValue, int steps);
    List<String> identifyMostSensitiveVariables(String planId);
    
    // Plan validation and optimization
    List<String> validatePlan(String planId);
    FinancialPlan optimizePlan(String planId);
    BigDecimal calculatePlanEfficiencyScore(String planId);
    
    // Benchmarking
    Map<String, BigDecimal> compareToBenchmarks(String planId);
    BigDecimal calculatePerformanceVsBenchmark(String planId, String benchmarkType);
}