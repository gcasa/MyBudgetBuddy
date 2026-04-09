package com.mybudgetbuddy.application.service;

import com.mybudgetbuddy.domain.model.FinancialPlan;
import com.mybudgetbuddy.domain.model.PlanStatus;
import com.mybudgetbuddy.domain.model.PlanType;
import java.util.List;
import java.util.Optional;

public interface PlanService {
    
    // Plan management
    FinancialPlan createPlan(String name, String userId, PlanType type);
    FinancialPlan updatePlan(FinancialPlan plan);
    void deletePlan(String planId);
    Optional<FinancialPlan> getPlanById(String planId);
    List<FinancialPlan> getPlansByUserId(String userId);
    List<FinancialPlan> getActivePlans(String userId);
    
    // Plan operations
    void activatePlan(String planId);
    void completePlan(String planId);
    void archivePlan(String planId);
    
    // Plan analysis
    boolean validatePlan(FinancialPlan plan);
    List<String> findPlanIssues(String planId);
    FinancialPlan duplicatePlan(String planId, String newName);
    
    // Budget integration
    void addBudgetToPlan(String planId, String budgetId);
    void removeBudgetFromPlan(String planId, String budgetId);
    
    // Goal integration 
    void addGoalToPlan(String planId, String goalId);
    void removeGoalFromPlan(String planId, String goalId);
    
    // Statistics
    long getPlanCount(String userId);
    FinancialPlan getMostRecentPlan(String userId);
    List<FinancialPlan> getPlansByStatus(String userId, PlanStatus status);
}