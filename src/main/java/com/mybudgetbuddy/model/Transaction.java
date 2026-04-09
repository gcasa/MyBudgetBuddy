package com.mybudgetbuddy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String planId;
    private String budgetId;
    private String goalId;  // Optional: link to specific goal
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String categoryId;
    private PaymentMethod paymentMethod;
    private LocalDate transactionDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    
    // Additional fields for comprehensive tracking
    private String vendor;
    private String location;
    private boolean isRecurring;
    private RecurringFrequency recurringFrequency;
    private String notes;
    private String receiptPath;
    private TransactionStatus status;
    private String tags; // Comma-separated tags for flexible categorization
    
    // For reconciliation
    private String accountNumber;
    private String referenceNumber;
    private boolean isReconciled;

    public Transaction() {
        this.id = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.status = TransactionStatus.CONFIRMED;
        this.isRecurring = false;
        this.isReconciled = false;
    }

    public Transaction(String description, BigDecimal amount, TransactionType type, String categoryId, LocalDate transactionDate) {
        this();
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.transactionDate = transactionDate;
    }
    
    // Business methods
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }
    
    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }
    
    public boolean isTransfer() {
        return type == TransactionType.TRANSFER;
    }
    
    public void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }
    
    public void markReconciled() {
        this.isReconciled = true;
        updateLastModified();
    }
    
    public void addTag(String tag) {
        if (tags == null || tags.isEmpty()) {
            tags = tag;
        } else {
            tags += "," + tag;
        }
        updateLastModified();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public String getBudgetId() { return budgetId; }
    public void setBudgetId(String budgetId) { this.budgetId = budgetId; }
    
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        updateLastModified();
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount;
        updateLastModified();
    }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { 
        this.type = type;
        updateLastModified();
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { 
        this.categoryId = categoryId;
        updateLastModified();
    }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { 
        this.paymentMethod = paymentMethod;
        updateLastModified();
    }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { 
        this.transactionDate = transactionDate;
        updateLastModified();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { 
        this.vendor = vendor;
        updateLastModified();
    }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { 
        this.location = location;
        updateLastModified();
    }
    
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { 
        this.isRecurring = recurring;
        updateLastModified();
    }
    
    public RecurringFrequency getRecurringFrequency() { return recurringFrequency; }
    public void setRecurringFrequency(RecurringFrequency recurringFrequency) { 
        this.recurringFrequency = recurringFrequency;
        updateLastModified();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes;
        updateLastModified();
    }
    
    public String getReceiptPath() { return receiptPath; }
    public void setReceiptPath(String receiptPath) { 
        this.receiptPath = receiptPath;
        updateLastModified();
    }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { 
        this.status = status;
        updateLastModified();
    }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { 
        this.tags = tags;
        updateLastModified();
    }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public boolean isReconciled() { return isReconciled; }
    public void setReconciled(boolean reconciled) { this.isReconciled = reconciled; }
    
    // Convenience methods for backward compatibility with ViewModels
    public LocalDate getDate() {
        return transactionDate;
    }
    
    public void setDate(LocalDate date) {
        setTransactionDate(date);
    }
    
    public String getCategory() {
        return categoryId;
    }
    
    public void setCategory(String categoryId) {
        setCategoryId(categoryId);
    }
}

enum PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    CHECK,
    BANK_TRANSFER,
    DIGITAL_WALLET,
    CRYPTOCURRENCY,
    OTHER
}

enum RecurringFrequency {
    DAILY,
    WEEKLY,
    BI_WEEKLY,
    MONTHLY,
    BI_MONTHLY,
    QUARTERLY,
    SEMI_ANNUALLY,
    ANNUALLY
}

enum TransactionStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    FAILED,
    REFUNDED
}
