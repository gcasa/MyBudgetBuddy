package com.mybudgetbuddy.application.service;

import com.mybudgetbuddy.domain.model.Goal;
import com.mybudgetbuddy.domain.model.Priority;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalService {
    
    // Goal management
    Goal createGoal(Goal goal);
    Goal updateGoal(Goal goal);
    void deleteGoal(String goalId);
    Optional<Goal> getGoalById(String goalId);
    List<Goal> getGoalsByPlanId(String planId);
    List<Goal> getActiveGoals(String planId);
    
    // Goal progress
    void updateProgress(String goalId, BigDecimal newAmount);
    void addContribution(String goalId, BigDecimal contributionAmount);
    BigDecimal getProgressPercentage(String goalId);
    BigDecimal getRemainingAmount(String goalId);
    long getDaysRemaining(String goalId);
    
    // Goal analysis
    BigDecimal getRequiredMonthlyContribution(String goalId);
    boolean isGoalOnTrack(String goalId);
    LocalDate getProjectedCompletionDate(String goalId);
    List<Goal> getGoalsAtRisk(String planId);
    List<Goal> getCompletedGoals(String planId);
    
    // Goal evaluation
    void evaluateGoal(String goalId);
    void evaluateAllGoals(String planId);
    List<String> getGoalRecommendations(String goalId);
    Goal suggestGoalAdjustment(String goalId);
    
    // Goal prioritization
    void setPriority(String goalId, Priority priority);
    List<Goal> getGoalsByPriority(String planId, Priority priority);
    List<Goal> getSortedGoalsByPriority(String planId);
    void reorderGoalPriorities(List<String> goalIds);
    
    // Goal templates and suggestions
    List<Goal> suggestGoalsBasedOnProfile(String planId, String userId);
    Goal createGoalFromTemplate(String templateName, String planId);
    List<String> getAvailableGoalTemplates();
    void saveGoalAsTemplate(String goalId, String templateName);
    
    // Goal tracking
    void linkGoalToTransactions(String goalId, List<String> transactionIds);
    List<String> getGoalLinkedTransactions(String goalId);
    void autoTrackGoalProgress(String goalId);
    BigDecimal calculateActualContributions(String goalId, LocalDate startDate, LocalDate endDate);
    
    // Goal milestones
    void addMilestone(String goalId, String description, BigDecimal amount, LocalDate date);
    List<Object> getGoalMilestones(String goalId); // Object represents milestone data structure
    boolean isMilestoneReached(String goalId, String milestoneId);
    void markMilestoneComplete(String goalId, String milestoneId);
    
    // Bulk operations
    void pauseGoal(String goalId);
    void resumeGoal(String goalId);
    void completeGoal(String goalId);
    void bulkUpdateGoals(List<Goal> goals);
    void archiveCompletedGoals(String planId);
    
    // Reporting
    BigDecimal getTotalGoalTargets(String planId);
    BigDecimal getTotalGoalProgress(String planId);
    BigDecimal getOverallGoalProgress(String planId);
    List<Goal> getGoalPerformanceSummary(String planId);
    
    // Integration
    void syncGoalWithBudget(String goalId, String budgetId);
    void createBudgetForGoal(String goalId);
    void adjustGoalBasedOnBudgetChanges(String goalId);
}