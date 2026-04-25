package com.mybudgetbuddy.application.service.impl;

import com.mybudgetbuddy.application.service.CategoryService;
import com.mybudgetbuddy.infrastructure.database.DatabaseManager;
import com.mybudgetbuddy.model.Category;
import com.mybudgetbuddy.model.CategoryType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLite implementation of CategoryService.
 */
public class CategoryServiceImpl implements CategoryService {
    
    private static final Logger LOGGER = Logger.getLogger(CategoryServiceImpl.class.getName());
    private final DatabaseManager databaseManager;
    
    public CategoryServiceImpl() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    @Override
    public Category createCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (category.getId() == null || category.getId().isEmpty()) {
            category.setId(UUID.randomUUID().toString());
        }

        // Avoid database-level primary key violations and provide a clearer error.
        if (categoryExists(category.getId())) {
            throw new IllegalArgumentException("Category already exists: " + category.getId());
        }
        
        String sql = """
            INSERT INTO categories (id, name, description, color, type)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getDescription());
            stmt.setString(4, category.getColor());
            stmt.setString(5, category.getType() != null ? category.getType().name() : null);
            
            stmt.executeUpdate();
            return category;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create category: " + category.getName(), e);
            throw new RuntimeException("Failed to create category", e);
        }
    }
    
    @Override
    public Category updateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (category.getId() == null) {
            throw new IllegalArgumentException("Category ID is required for update");
        }
        
        String sql = """
            UPDATE categories SET 
                name = ?, description = ?, color = ?, type = ?
            WHERE id = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getColor());
            stmt.setString(4, category.getType() != null ? category.getType().name() : null);
            stmt.setString(5, category.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Category not found for update: " + category.getId());
            }
            
            return category;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }
    
    @Override
    public void deleteCategory(String categoryId) {
        // Check if category is being used by transactions
        long usageCount = getCategoryUsageCount(categoryId);
        if (usageCount > 0) {
            throw new IllegalStateException(
                "Cannot delete category: it is being used by " + usageCount + " transactions"
            );
        }
        
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category", e);
        }
    }
    
    @Override
    public Optional<Category> getCategoryById(String categoryId) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToCategory(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get category by ID", e);
        }
    }
    
    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
            return categories;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all categories", e);
        }
    }
    
    @Override
    public List<Category> getCategoriesByType(CategoryType type) {
        String sql = "SELECT * FROM categories WHERE type = ? ORDER BY name";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.name());
            
            ResultSet rs = stmt.executeQuery();
            List<Category> categories = new ArrayList<>();
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
            return categories;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get categories by type", e);
        }
    }
    
    @Override
    public List<Category> getIncomeCategories() {
        return getCategoriesByType(CategoryType.INCOME);
    }
    
    @Override
    public List<Category> getExpenseCategories() {
        return getCategoriesByType(CategoryType.EXPENSE);
    }
    
    @Override
    public boolean categoryExists(String categoryId) {
        return getCategoryById(categoryId).isPresent();
    }
    
    @Override
    public long getCategoryUsageCount(String categoryId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE category_id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            rs.next();
            return rs.getLong(1);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get category usage count", e);
        }
    }
    
    /**
     * Helper method to map ResultSet to Category object
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        
        category.setId(rs.getString("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setColor(rs.getString("color"));
        
        String type = rs.getString("type");
        if (type != null) {
            category.setType(CategoryType.valueOf(type));
        }
        
        return category;
    }
}