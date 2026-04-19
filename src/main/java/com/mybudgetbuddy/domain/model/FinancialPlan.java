package com.mybudgetbuddy.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FinancialPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String userId;
    private String name;
    private String description;
    private PlanType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private PlanStatus status;
    
    // Financial data
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal targetSavings;
    private BigDecimal emergencyFundTarget;
    
    // Associated entities
    private List<String> budgetIds;
    private List<String> goalIds;
    private List<String> scenarioIds;
    
    // Metadata
    private LocalDate createdDate;
    private LocalDate lastModified;
    private String lastModifiedBy;
    
    public FinancialPlan() {
        this.id = UUID.randomUUID().toString();
        this.status = PlanStatus.DRAFT;
        this.budgetIds = new ArrayList<>();
        this.goalIds = new ArrayList<>();
        this.scenarioIds = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.lastModified = LocalDate.now();
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.targetSavings = BigDecimal.ZERO;
    }
    
    public FinancialPlan(String name, String userId, PlanType type) {
        this();
        this.name = name;
        this.userId = userId;
        this.type = type;
        this.startDate = LocalDate.now();
    }
    
    // Calculated properties
    public BigDecimal getNetIncome() {
        return totalIncome.subtract(totalExpenses);
    }
    
    public BigDecimal getSavingsRate() {
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return targetSavings.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }
    
    public boolean isHealthy() {
        return getNetIncome().compareTo(BigDecimal.ZERO) > 0 && 
               getSavingsRate().compareTo(new BigDecimal("10")) >= 0;
    }
    
    public long getDaysRemaining() {
        if (endDate == null) return 0;
        return LocalDate.now().until(endDate).getDays();
    }
    
    // Methods
    public void addBudgetId(String budgetId) {
        if (!budgetIds.contains(budgetId)) {
            budgetIds.add(budgetId);
            updateLastModified();
        }
    }
    
    public void removeBudgetId(String budgetId) {
        budgetIds.remove(budgetId);
        updateLastModified();
    }
    
    public void addGoalId(String goalId) {
        if (!goalIds.contains(goalId)) {
            goalIds.add(goalId);
            updateLastModified();
        }
    }
    
    public void removeGoalId(String goalId) {
        goalIds.remove(goalId);
        updateLastModified();
    }
    
    public void addScenarioId(String scenarioId) {
        if (!scenarioIds.contains(scenarioId)) {
            scenarioIds.add(scenarioId);
            updateLastModified();
        }
    }
    
    public void activate() {
        this.status = PlanStatus.ACTIVE;
        updateLastModified();
    }
    
    public void complete() {
        this.status = PlanStatus.COMPLETED;
        updateLastModified();
    }
    
    public void archive() {
        this.status = PlanStatus.ARCHIVED;
        updateLastModified();
    }
    
    private void updateLastModified() {
        this.lastModified = LocalDate.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        updateLastModified();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        updateLastModified();
    }
    
    public PlanType getType() { return type; }
    public void setType(PlanType type) { this.type = type; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
    
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { 
        this.totalIncome = totalIncome;
        updateLastModified();
    }
    
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { 
        this.totalExpenses = totalExpenses;
        updateLastModified();
    }
    
    public BigDecimal getTargetSavings() { return targetSavings; }
    public void setTargetSavings(BigDecimal targetSavings) { 
        this.targetSavings = targetSavings;
        updateLastModified();
    }
    
    public BigDecimal getEmergencyFundTarget() { return emergencyFundTarget; }
    public void setEmergencyFundTarget(BigDecimal emergencyFundTarget) { 
        this.emergencyFundTarget = emergencyFundTarget;
        updateLastModified();
    }
    
    public List<String> getBudgetIds() { return new ArrayList<>(budgetIds); }
    public void setBudgetIds(List<String> budgetIds) { this.budgetIds = new ArrayList<>(budgetIds); }
    
    public List<String> getGoalIds() { return new ArrayList<>(goalIds); }
    public void setGoalIds(List<String> goalIds) { this.goalIds = new ArrayList<>(goalIds); }
    
    public List<String> getScenarioIds() { return new ArrayList<>(scenarioIds); }
    public void setScenarioIds(List<String> scenarioIds) { this.scenarioIds = new ArrayList<>(scenarioIds); }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public LocalDate getLastModified() { return lastModified; }
    public void setLastModified(LocalDate lastModified) { this.lastModified = lastModified; }
    
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
}