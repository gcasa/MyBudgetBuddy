package com.mybudgetbuddy.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Scenario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String planId;
    private String name;
    private String description;
    private ScenarioType type;
    
    // Scenario parameters
    private Map<String, BigDecimal> incomeAdjustments; // Category -> adjustment amount
    private Map<String, BigDecimal> expenseAdjustments; // Category -> adjustment amount
    private Map<String, BigDecimal> goalAdjustments; // Goal -> adjustment amount
    private BigDecimal inflationRate;
    private BigDecimal investmentReturnRate;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Results (calculated after running scenario)
    private BigDecimal projectedIncome;
    private BigDecimal projectedExpenses;
    private BigDecimal projectedSavings;
    private BigDecimal projectedNetWorth;
    private Map<String, BigDecimal> goalCompletionProbabilities; // Goal -> probability percentage
    private List<String> warnings;
    private List<String> recommendations;
    
    // Metadata
    private ScenarioStatus status;
    private LocalDateTime lastRunDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private boolean isBaseline; // Is this the baseline/current scenario
    
    public Scenario() {
        this.id = UUID.randomUUID().toString();
        this.incomeAdjustments = new HashMap<>();
        this.expenseAdjustments = new HashMap<>();
        this.goalAdjustments = new HashMap<>();
        this.goalCompletionProbabilities = new HashMap<>();
        this.warnings = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.status = ScenarioStatus.DRAFT;
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.isBaseline = false;
        this.inflationRate = new BigDecimal("0.03"); // Default 3%
        this.investmentReturnRate = new BigDecimal("0.07"); // Default 7%
    }
    
    public Scenario(String name, String planId, ScenarioType type) {
        this();
        this.name = name;
        this.planId = planId;
        this.type = type;
    }
    
    // Business methods
    public void addIncomeAdjustment(String category, BigDecimal adjustment) {
        incomeAdjustments.put(category, adjustment);
        updateLastModified();
    }
    
    public void addExpenseAdjustment(String category, BigDecimal adjustment) {
        expenseAdjustments.put(category, adjustment);
        updateLastModified();
    }
    
    public void addGoalAdjustment(String goalId, BigDecimal adjustment) {
        goalAdjustments.put(goalId, adjustment);
        updateLastModified();
    }
    
    public BigDecimal getNetProjectedIncome() {
        if (projectedIncome == null || projectedExpenses == null) {
            return BigDecimal.ZERO;
        }
        return projectedIncome.subtract(projectedExpenses);
    }
    
    public void addWarning(String warning) {
        warnings.add(warning);
        updateLastModified();
    }
    
    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
        updateLastModified();
    }
    
    public void markAsRun() {
        this.status = ScenarioStatus.COMPLETED;
        this.lastRunDate = LocalDateTime.now();
        updateLastModified();
    }
    
    public void setAsBaseline() {
        this.isBaseline = true;
        updateLastModified();
    }
    
    public boolean hasBeenRun() {
        return lastRunDate != null;
    }
    
    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
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
    
    public ScenarioType getType() { return type; }
    public void setType(ScenarioType type) { this.type = type; }
    
    public Map<String, BigDecimal> getIncomeAdjustments() { return new HashMap<>(incomeAdjustments); }
    public void setIncomeAdjustments(Map<String, BigDecimal> incomeAdjustments) { 
        this.incomeAdjustments = new HashMap<>(incomeAdjustments);
        updateLastModified();
    }
    
    public Map<String, BigDecimal> getExpenseAdjustments() { return new HashMap<>(expenseAdjustments); }
    public void setExpenseAdjustments(Map<String, BigDecimal> expenseAdjustments) { 
        this.expenseAdjustments = new HashMap<>(expenseAdjustments);
        updateLastModified();
    }
    
    public Map<String, BigDecimal> getGoalAdjustments() { return new HashMap<>(goalAdjustments); }
    public void setGoalAdjustments(Map<String, BigDecimal> goalAdjustments) { 
        this.goalAdjustments = new HashMap<>(goalAdjustments);
        updateLastModified();
    }
    
    public BigDecimal getInflationRate() { return inflationRate; }
    public void setInflationRate(BigDecimal inflationRate) { 
        this.inflationRate = inflationRate;
        updateLastModified();
    }
    
    public BigDecimal getInvestmentReturnRate() { return investmentReturnRate; }
    public void setInvestmentReturnRate(BigDecimal investmentReturnRate) { 
        this.investmentReturnRate = investmentReturnRate;
        updateLastModified();
    }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate;
        updateLastModified();
    }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate;
        updateLastModified();
    }
    
    public BigDecimal getProjectedIncome() { return projectedIncome; }
    public void setProjectedIncome(BigDecimal projectedIncome) { this.projectedIncome = projectedIncome; }
    
    public BigDecimal getProjectedExpenses() { return projectedExpenses; }
    public void setProjectedExpenses(BigDecimal projectedExpenses) { this.projectedExpenses = projectedExpenses; }
    
    public BigDecimal getProjectedSavings() { return projectedSavings; }
    public void setProjectedSavings(BigDecimal projectedSavings) { this.projectedSavings = projectedSavings; }
    
    public BigDecimal getProjectedNetWorth() { return projectedNetWorth; }
    public void setProjectedNetWorth(BigDecimal projectedNetWorth) { this.projectedNetWorth = projectedNetWorth; }
    
    public Map<String, BigDecimal> getGoalCompletionProbabilities() { return new HashMap<>(goalCompletionProbabilities); }
    public void setGoalCompletionProbabilities(Map<String, BigDecimal> goalCompletionProbabilities) { 
        this.goalCompletionProbabilities = new HashMap<>(goalCompletionProbabilities);
    }
    
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
    public void setWarnings(List<String> warnings) { this.warnings = new ArrayList<>(warnings); }
    
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
    public void setRecommendations(List<String> recommendations) { this.recommendations = new ArrayList<>(recommendations); }
    
    public ScenarioStatus getStatus() { return status; }
    public void setStatus(ScenarioStatus status) { this.status = status; }
    
    public LocalDateTime getLastRunDate() { return lastRunDate; }
    public void setLastRunDate(LocalDateTime lastRunDate) { this.lastRunDate = lastRunDate; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public boolean isBaseline() { return isBaseline; }
    public void setBaseline(boolean baseline) { this.isBaseline = baseline; }
}

enum ScenarioType {
    BASELINE,
    OPTIMISTIC,
    PESSIMISTIC,
    CUSTOM,
    STRESS_TEST,
    WHAT_IF,
    RETIREMENT,
    EMERGENCY
}

enum ScenarioStatus {
    DRAFT,
    RUNNING,
    COMPLETED,
    FAILED,
    OUTDATED
}