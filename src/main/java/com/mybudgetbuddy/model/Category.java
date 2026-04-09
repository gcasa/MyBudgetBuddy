package com.mybudgetbuddy.model;

import java.io.Serializable;
import java.util.UUID;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String description;
    private String color;
    private CategoryType type;
    
    public Category() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Category(String name, CategoryType type) {
        this();
        this.name = name;
        this.type = type;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    
    @Override
    public String toString() {
        return name;
    }
}