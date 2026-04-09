package com.mybudgetbuddy.application.service;

import com.mybudgetbuddy.model.Category;
import com.mybudgetbuddy.model.CategoryType;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing budget categories.
 */
public interface CategoryService {
    
    // Core CRUD operations
    Category createCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(String categoryId);
    Optional<Category> getCategoryById(String categoryId);
    
    // Query operations
    List<Category> getAllCategories();
    List<Category> getCategoriesByType(CategoryType type);
    List<Category> getIncomeCategories();
    List<Category> getExpenseCategories();
    
    // Utility methods
    boolean categoryExists(String categoryId);
    long getCategoryUsageCount(String categoryId);
}