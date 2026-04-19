package com.mybudgetbuddy.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String planId;
    private String categoryId;
    private String name;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;
    private Period budgetPeriod; // Monthly, Quarterly, Yearly
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetType type;
    private BigDecimal warningThreshold; // Percentage of budget (e.g., 0.8 for 80%)
    private boolean isActive;
    private LocalDate createdDate;
    private LocalDate lastModified;
    
    public Budget() {
        this.id = UUID.randomUUID().toString();
        this.spentAmount = BigDecimal.ZERO;
        this.warningThreshold = new BigDecimal("0.8");
        this.isActive = true;
        this.createdDate = LocalDate.now();
        this.lastModified = LocalDate.now();
    }
    
    public Budget(String name, String categoryId, BigDecimal allocatedAmount, Period budgetPeriod) {
        this();
        this.name = name;
        this.categoryId = categoryId;
        this.allocatedAmount = allocatedAmount;
        this.budgetPeriod = budgetPeriod;
    }
    
    // Calculated properties
    public BigDecimal getRemainingAmount() {
        return allocatedAmount.subtract(spentAmount);
    }
    
    public BigDecimal getUsagePercentage() {
        if (allocatedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.divide(allocatedAmount, 4, RoundingMode.HALF_UP);
    }
    
    public boolean isOverBudget() {
        return spentAmount.compareTo(allocatedAmount) > 0;
    }
    
    public boolean isNearWarningThreshold() {
        return getUsagePercentage().compareTo(warningThreshold) >= 0;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { 
        this.allocatedAmount = allocatedAmount;
        this.lastModified = LocalDate.now();
    }
    
    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { 
        this.spentAmount = spentAmount;
        this.lastModified = LocalDate.now();
    }
    
    public Period getBudgetPeriod() { return budgetPeriod; }
    public void setBudgetPeriod(Period budgetPeriod) { this.budgetPeriod = budgetPeriod; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public BudgetType getType() { return type; }
    public void setType(BudgetType type) { this.type = type; }
    
    public BigDecimal getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(BigDecimal warningThreshold) { this.warningThreshold = warningThreshold; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public LocalDate getLastModified() { return lastModified; }
    public void setLastModified(LocalDate lastModified) { this.lastModified = lastModified; }
}