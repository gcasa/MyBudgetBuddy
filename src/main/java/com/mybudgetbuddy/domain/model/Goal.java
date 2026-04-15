package com.mybudgetbuddy.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Goal implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String planId;
    private String name;
    private String description;
    private GoalType type;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private Priority priority;
    private GoalStatus status;
    private BigDecimal monthlyContribution;
    private LocalDate createdDate;
    private LocalDate lastUpdated;
    
    public Goal() {
        this.id = UUID.randomUUID().toString();
        this.currentAmount = BigDecimal.ZERO;
        this.status = GoalStatus.ACTIVE;
        this.priority = Priority.MEDIUM;
        this.createdDate = LocalDate.now();
        this.lastUpdated = LocalDate.now();
    }
    
    public Goal(String name, GoalType type, BigDecimal targetAmount, LocalDate targetDate) {
        this();
        this.name = name;
        this.type = type;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
    }
    
    // Calculated properties
    public BigDecimal getProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }
    
    public BigDecimal getRemainingAmount() {
        return targetAmount.subtract(currentAmount);
    }
    
    public boolean isCompleted() {
        return currentAmount.compareTo(targetAmount) >= 0;
    }
    
    public long getDaysRemaining() {
        if (targetDate == null) return 0;
        return LocalDate.now().until(targetDate).getDays();
    }
    
    public BigDecimal getRequiredMonthlyContribution() {
        long monthsRemaining = LocalDate.now().until(targetDate).toTotalMonths();
        if (monthsRemaining <= 0) return getRemainingAmount();
        return getRemainingAmount().divide(new BigDecimal(monthsRemaining), 2, java.math.RoundingMode.UP);
    }
    
    // Methods
    public void addContribution(BigDecimal amount) {
        currentAmount = currentAmount.add(amount);
        lastUpdated = LocalDate.now();
        updateStatus();
    }
    
    public void updateProgress(BigDecimal newAmount) {
        currentAmount = newAmount;
        lastUpdated = LocalDate.now();
        updateStatus();
    }
    
    private void updateStatus() {
        if (isCompleted()) {
            status = GoalStatus.COMPLETED;
        } else if (targetDate != null && LocalDate.now().isAfter(targetDate)) {
            status = GoalStatus.OVERDUE;
        } else {
            status = GoalStatus.ACTIVE;
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public GoalType getType() { return type; }
    public void setType(GoalType type) { this.type = type; }
    
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    
    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }
    
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }
    
    public BigDecimal getMonthlyContribution() { return monthlyContribution; }
    public void setMonthlyContribution(BigDecimal monthlyContribution) { this.monthlyContribution = monthlyContribution; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
}