package com.mybudgetbuddy.model;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String description;
    private double amount;
    private TransactionType type;
    private String category;
    private LocalDate date;

    public Transaction() {
        this.id = UUID.randomUUID().toString();
    }

    public Transaction(String description, double amount, TransactionType type, String category, LocalDate date) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
