package com.mybudgetbuddy.engine;

import com.mybudgetbuddy.domain.model.Recommendation;
import com.mybudgetbuddy.domain.model.RecommendationType;
import com.mybudgetbuddy.domain.model.Priority;
import com.mybudgetbuddy.domain.model.FinancialPlan;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RulesRecommendationEngine {
    
    // Rule evaluation
    List<Recommendation> evaluateRules(String planId);
    List<Recommendation> evaluateRulesForCategory(String planId, String categoryId);
    List<Recommendation> evaluateRulesForGoal(String goalId);
    List<Recommendation> evaluateRulesForBudget(String budgetId);
    
    // Spending analysis
    List<Recommendation> detectOverspending(String planId);
    List<Recommendation> identifySpendingPatterns(String planId);
    List<Recommendation> findUnusualTransactions(String planId);
    List<Recommendation> analyzeRecurringExpenses(String planId);
    
    // Financial health assessment
    BigDecimal calculateFinancialHealthScore(String planId);
    List<Recommendation> generateHealthRecommendations(String planId);
    List<String> identifyFinancialRedFlags(String planId);
    
    // Budget recommendations
    List<Recommendation> generateBudgetRecommendations(String planId);
    Recommendation recommendBudgetAdjustment(String budgetId);
    List<Recommendation> suggestBudgetCategories(String planId);
    Recommendation optimizeBudgetAllocation(String planId);
    
    // Savings recommendations
    List<Recommendation> generateSavingsRecommendations(String planId);
    Recommendation calculateOptimalSavingsRate(String planId);
    List<Recommendation> identifySavingsOpportunities(String planId);
    
    // Goal recommendations
    List<Recommendation> generateGoalRecommendations(String planId);
    Recommendation adjustGoalTarget(String goalId);
    Recommendation adjustGoalTimeline(String goalId);
    List<Recommendation> prioritizeGoals(String planId);
    
    // Investment recommendations
    List<Recommendation> generateInvestmentAdvice(String planId);
    Recommendation recommendAssetAllocation(String planId, BigDecimal riskTolerance);
    List<Recommendation> identifyInvestmentGaps(String planId);
    
    // Debt management recommendations
    List<Recommendation> generateDebtRecommendations(String planId);
    Recommendation recommendDebtPayoffStrategy(String planId);
    List<Recommendation> identifyDebtConsolidationOpportunities(String planId);
    
    // Emergency fund recommendations
    List<Recommendation> evaluateEmergencyFund(String planId);
    Recommendation calculateOptimalEmergencyFund(String planId);
    
    // Tax optimization
    List<Recommendation> generateTaxRecommendations(String planId);
    List<Recommendation> identifyTaxSavingOpportunities(String planId);
    
    // Cash flow optimization
    List<Recommendation> optimizeCashFlow(String planId);
    List<Recommendation> identifyIncomeOpportunities(String planId);
    List<Recommendation> suggestExpenseReductions(String planId);
    
    // Risk management
    List<Recommendation> assessInsuranceNeeds(String planId);
    List<Recommendation> identifyFinancialRisks(String planId);
    List<Recommendation> recommendRiskMitigation(String planId);
    
    // Behavioral finance
    List<Recommendation> identifySpendingTriggers(String planId);
    List<Recommendation> suggestBehavioralChanges(String planId);
    BigDecimal calculateImpulsePurchaseImpact(String planId);
    
    // Personalized recommendations
    List<Recommendation> generatePersonalizedAdvice(String planId, String userId);
    List<Recommendation> getLifeStageRecommendations(String planId, String lifeStage);
    List<Recommendation> getSeasonalRecommendations(String planId);
    
    // Performance tracking
    void trackRecommendationEffectiveness(String recommendationId, boolean implemented);
    BigDecimal calculateRecommendationROI(String recommendationId);
    List<Recommendation> getMostEffectiveRecommendations(String planId);
    
    // Rule management
    void addCustomRule(String ruleName, String condition, String action, Priority priority);
    void updateRule(String ruleId, String condition, String action);
    void deactivateRule(String ruleId);
    List<String> getActiveRules();
    
    // Machine learning and pattern recognition
    List<Recommendation> generateMLRecommendations(String planId);
    void trainRecommendationModel(List<String> planIds);
    Map<String, Double> getRecommendationConfidenceScores(String planId);
    
    // Comparative analysis
    List<Recommendation> compareToPeers(String planId, Map<String, Object> demographicData);
    List<Recommendation> benchmarkAgainstBestPractices(String planId);
    
    // Alert generation
    List<Recommendation> generateCriticalAlerts(String planId);
    List<Recommendation> generateWarnings(String planId);
    List<Recommendation> generateOpportunityAlerts(String planId);
}