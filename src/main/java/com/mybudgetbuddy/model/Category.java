package com.mybudgetbuddy.model;

import java.util.UUID;

public class Category implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private TransactionType type;

    public Category(String name, TransactionType type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public TransactionType getType() { return type; }

    @Override
    public String toString() { return name; }
}
