package com.mybudgetbuddy.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recommendation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String planId;
    private String userId;
    private RecommendationType type;
    private Priority priority;
    private String title;
    private String description;
    private String actionText; // What the user should do
    
    // Financial impact
    private BigDecimal potentialSavings;
    private BigDecimal costToImplement;
    private BigDecimal timeToBreakEven; // Months
    private BigDecimal riskLevel; // 0-1 scale
    
    // Related entities
    private List<String> relatedGoalIds;
    private List<String> relatedBudgetIds;
    private List<String> relatedCategoryIds;
    
    // Supporting data
    private List<String> reasons; // Why this recommendation was made
    private List<String> benefits; // Expected benefits
    private List<String> risks; // Potential risks
    private List<String> steps; // Implementation steps
    private String sourceRule; // Which rule generated this
    
    // Status tracking
    private RecommendationStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime expiryDate; // When recommendation becomes stale
    private LocalDateTime implementedDate;
    private LocalDateTime dismissedDate;
    private String dismissalReason;
    private String userFeedback;
    private int userRating; // 1-5 stars
    
    public Recommendation() {
        this.id = UUID.randomUUID().toString();
        this.priority = Priority.MEDIUM;
        this.status = RecommendationStatus.NEW;
        this.createdDate = LocalDateTime.now();
        this.relatedGoalIds = new ArrayList<>();
        this.relatedBudgetIds = new ArrayList<>();
        this.relatedCategoryIds = new ArrayList<>();
        this.reasons = new ArrayList<>();
        this.benefits = new ArrayList<>();
        this.risks = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.potentialSavings = BigDecimal.ZERO;
        this.costToImplement = BigDecimal.ZERO;
        this.riskLevel = BigDecimal.ZERO;
    }
    
    public Recommendation(String title, RecommendationType type, Priority priority, String description) {
        this();
        this.title = title;
        this.type = type;
        this.priority = priority;
        this.description = description;
    }
    
    // Business methods
    public boolean isActive() {
        return status == RecommendationStatus.NEW || status == RecommendationStatus.VIEWED;
    }
    
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
    
    public void markAsViewed() {
        if (status == RecommendationStatus.NEW) {
            this.status = RecommendationStatus.VIEWED;
        }
    }
    
    public void implement() {
        this.status = RecommendationStatus.IMPLEMENTED;
        this.implementedDate = LocalDateTime.now();
    }
    
    public void dismiss(String reason) {
        this.status = RecommendationStatus.DISMISSED;
        this.dismissedDate = LocalDateTime.now();
        this.dismissalReason = reason;
    }
    
    public void addReason(String reason) {
        this.reasons.add(reason);
    }
    
    public void addBenefit(String benefit) {
        this.benefits.add(benefit);
    }
    
    public void addRisk(String risk) {
        this.risks.add(risk);
    }
    
    public void addStep(String step) {
        this.steps.add(step);
    }
    
    public void addRelatedGoalId(String goalId) {
        if (!relatedGoalIds.contains(goalId)) {
            relatedGoalIds.add(goalId);
        }
    }
    
    public void addRelatedBudgetId(String budgetId) {
        if (!relatedBudgetIds.contains(budgetId)) {
            relatedBudgetIds.add(budgetId);
        }
    }
    
    public void addRelatedCategoryId(String categoryId) {
        if (!relatedCategoryIds.contains(categoryId)) {
            relatedCategoryIds.add(categoryId);
        }
    }
    
    public BigDecimal getReturnOnInvestment() {
        if (costToImplement.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return potentialSavings.divide(costToImplement, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public boolean isHighImpact() {
        return potentialSavings.compareTo(new BigDecimal("1000")) > 0 || 
               getReturnOnInvestment().compareTo(new BigDecimal("5")) > 0;
    }
    
    public boolean isLowRisk() {
        return riskLevel.compareTo(new BigDecimal("0.3")) <= 0;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public RecommendationType getType() { return type; }
    public void setType(RecommendationType type) { this.type = type; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }
    
    public BigDecimal getPotentialSavings() { return potentialSavings; }
    public void setPotentialSavings(BigDecimal potentialSavings) { this.potentialSavings = potentialSavings; }
    
    public BigDecimal getCostToImplement() { return costToImplement; }
    public void setCostToImplement(BigDecimal costToImplement) { this.costToImplement = costToImplement; }
    
    public BigDecimal getTimeToBreakEven() { return timeToBreakEven; }
    public void setTimeToBreakEven(BigDecimal timeToBreakEven) { this.timeToBreakEven = timeToBreakEven; }
    
    public BigDecimal getRiskLevel() { return riskLevel; }
    public void setRiskLevel(BigDecimal riskLevel) { this.riskLevel = riskLevel; }
    
    public List<String> getRelatedGoalIds() { return new ArrayList<>(relatedGoalIds); }
    public void setRelatedGoalIds(List<String> relatedGoalIds) { this.relatedGoalIds = new ArrayList<>(relatedGoalIds); }
    
    public List<String> getRelatedBudgetIds() { return new ArrayList<>(relatedBudgetIds); }
    public void setRelatedBudgetIds(List<String> relatedBudgetIds) { this.relatedBudgetIds = new ArrayList<>(relatedBudgetIds); }
    
    public List<String> getRelatedCategoryIds() { return new ArrayList<>(relatedCategoryIds); }
    public void setRelatedCategoryIds(List<String> relatedCategoryIds) { this.relatedCategoryIds = new ArrayList<>(relatedCategoryIds); }
    
    public List<String> getReasons() { return new ArrayList<>(reasons); }
    public void setReasons(List<String> reasons) { this.reasons = new ArrayList<>(reasons); }
    
    public List<String> getBenefits() { return new ArrayList<>(benefits); }
    public void setBenefits(List<String> benefits) { this.benefits = new ArrayList<>(benefits); }
    
    public List<String> getRisks() { return new ArrayList<>(risks); }
    public void setRisks(List<String> risks) { this.risks = new ArrayList<>(risks); }
    
    public List<String> getSteps() { return new ArrayList<>(steps); }
    public void setSteps(List<String> steps) { this.steps = new ArrayList<>(steps); }
    
    public String getSourceRule() { return sourceRule; }
    public void setSourceRule(String sourceRule) { this.sourceRule = sourceRule; }
    
    public RecommendationStatus getStatus() { return status; }
    public void setStatus(RecommendationStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDateTime getImplementedDate() { return implementedDate; }
    public void setImplementedDate(LocalDateTime implementedDate) { this.implementedDate = implementedDate; }
    
    public LocalDateTime getDismissedDate() { return dismissedDate; }
    public void setDismissedDate(LocalDateTime dismissedDate) { this.dismissedDate = dismissedDate; }
    
    public String getDismissalReason() { return dismissalReason; }
    public void setDismissalReason(String dismissalReason) { this.dismissalReason = dismissalReason; }
    
    public String getUserFeedback() { return userFeedback; }
    public void setUserFeedback(String userFeedback) { this.userFeedback = userFeedback; }
    
    public int getUserRating() { return userRating; }
    public void setUserRating(int userRating) { this.userRating = Math.max(1, Math.min(5, userRating)); }
}

enum RecommendationType {
    BUDGET_OPTIMIZATION,
    EXPENSE_REDUCTION,
    INCOME_IMPROVEMENT,
    DEBT_MANAGEMENT,
    SAVINGS_OPTIMIZATION,
    INVESTMENT_ADVICE,
    GOAL_ADJUSTMENT,
    EMERGENCY_FUND,
    TAX_OPTIMIZATION,
    CASH_FLOW_IMPROVEMENT,
    RISK_MITIGATION,
    ACCOUNT_MANAGEMENT
}

enum RecommendationStatus {
    NEW,
    VIEWED,
    IMPLEMENTED,
    DISMISSED,
    EXPIRED
}