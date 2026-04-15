package com.mybudgetbuddy.application.service.impl;

import com.mybudgetbuddy.application.service.GoalService;
import com.mybudgetbuddy.domain.model.*;
import com.mybudgetbuddy.infrastructure.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLite implementation of GoalService.
 */
public class GoalServiceImpl implements GoalService {
    
    private static final Logger LOGGER = Logger.getLogger(GoalServiceImpl.class.getName());
    private final DatabaseManager databaseManager;
    
    public GoalServiceImpl() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    @Override
    public Goal createGoal(Goal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("Goal cannot be null");
        }
        
        if (goal.getId() == null || goal.getId().isEmpty()) {
            goal.setId(UUID.randomUUID().toString());
        }
        
        String sql = """
            INSERT INTO goals (id, name, description, type, target_amount, current_amount, 
                             target_date, created_date, priority, monthly_contribution, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, goal.getId());
            stmt.setString(2, goal.getName());
            stmt.setString(3, goal.getDescription());
            stmt.setString(4, goal.getType() != null ? goal.getType().name() : null);
            stmt.setBigDecimal(5, goal.getTargetAmount());
            stmt.setBigDecimal(6, goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
            stmt.setDate(7, goal.getTargetDate() != null ? Date.valueOf(goal.getTargetDate()) : null);
            stmt.setDate(8, goal.getCreatedDate() != null ? Date.valueOf(goal.getCreatedDate()) : Date.valueOf(LocalDate.now()));
            stmt.setString(9, goal.getPriority() != null ? goal.getPriority().name() : null);
            stmt.setBigDecimal(10, goal.getMonthlyContribution());
            stmt.setString(11, goal.getStatus() != null ? goal.getStatus().name() : GoalStatus.ACTIVE.name());
            
            stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Created goal: {0}", goal.getName());
            
            return goal;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create goal: " + goal.getName(), e);
            throw new RuntimeException("Failed to create goal", e);
        }
    }
    
    @Override
    public Optional<Goal> getGoalById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE id = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(buildGoalFromResultSet(rs));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get goal by ID: " + id, e);
            throw new RuntimeException("Failed to get goal", e);
        }
    }
    
    public List<Goal> getAllGoals() {
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals ORDER BY created_date DESC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get all goals", e);
            throw new RuntimeException("Failed to get goals", e);
        }
    }
    
    public List<Goal> getGoalsByType(GoalType type) {
        if (type == null) {
            return getAllGoals();
        }
        
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE type = ? ORDER BY created_date DESC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get goals by type: " + type, e);
            throw new RuntimeException("Failed to get goals by type", e);
        }
    }
    
    public List<Goal> getGoalsByStatus(GoalStatus status) {
        if (status == null) {
            return getAllGoals();
        }
        
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE status = ? ORDER BY created_date DESC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get goals by status: " + status, e);
            throw new RuntimeException("Failed to get goals by status", e);
        }
    }
    
    public List<Goal> getActiveGoals() {
        return getGoalsByStatus(GoalStatus.ACTIVE);
    }
    
    @Override
    public List<Goal> getActiveGoals(String planId) {
        // For now, return all active goals since plan integration isn't implemented
        return getGoalsByStatus(GoalStatus.ACTIVE);
    }
    
    @Override
    public List<Goal> getGoalsByPlanId(String planId) {
        // For now, return all goals since plan integration isn't implemented
        return getAllGoals();
    }
    
    public List<Goal> getCompletedGoals() {
        return getGoalsByStatus(GoalStatus.COMPLETED);
    }
    
    @Override
    public Goal updateGoal(Goal goal) {
        if (goal == null || goal.getId() == null) {
            throw new IllegalArgumentException("Goal and goal ID cannot be null");
        }
        
        String sql = """
            UPDATE goals SET name = ?, description = ?, type = ?, target_amount = ?, 
                           current_amount = ?, target_date = ?, priority = ?, 
                           monthly_contribution = ?, status = ?
            WHERE id = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, goal.getName());
            stmt.setString(2, goal.getDescription());
            stmt.setString(3, goal.getType() != null ? goal.getType().name() : null);
            stmt.setBigDecimal(4, goal.getTargetAmount());
            stmt.setBigDecimal(5, goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
            stmt.setDate(6, goal.getTargetDate() != null ? Date.valueOf(goal.getTargetDate()) : null);
            stmt.setString(7, goal.getPriority() != null ? goal.getPriority().name() : null);
            stmt.setBigDecimal(8, goal.getMonthlyContribution());
            stmt.setString(9, goal.getStatus() != null ? goal.getStatus().name() : GoalStatus.ACTIVE.name());
            stmt.setString(10, goal.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Goal not found: " + goal.getId());
            }
            
            LOGGER.log(Level.INFO, "Updated goal: {0}", goal.getName());
            
            return goal;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update goal: " + goal.getName(), e);
            throw new RuntimeException("Failed to update goal", e);
        }
    }
    
    @Override
    public void deleteGoal(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal ID cannot be null or empty");
        }
        
        String sql = "DELETE FROM goals WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Goal not found: " + id);
            }
            
            LOGGER.log(Level.INFO, "Deleted goal: {0}", id);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete goal: " + id, e);
            throw new RuntimeException("Failed to delete goal", e);
        }
    }
    
    @Override
    public void addContribution(String goalId, BigDecimal amount) {
        if (goalId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Goal ID and positive amount are required");
        }
        
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new RuntimeException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);
        
        // Check if goal is now completed
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        
        updateGoal(goal);
    }
    
    public Goal markGoalComplete(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new RuntimeException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        goal.setStatus(GoalStatus.COMPLETED);
        goal.setCurrentAmount(goal.getTargetAmount());
        
        return updateGoal(goal);
    }
    
    @Override
    public void pauseGoal(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new RuntimeException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        goal.setStatus(GoalStatus.PAUSED);
        
        updateGoal(goal);
    }
    
    @Override
    public void resumeGoal(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new RuntimeException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        
        // Determine appropriate status based on progress and date
        if (goal.getTargetDate() != null && goal.getTargetDate().isBefore(LocalDate.now()) 
            && goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(GoalStatus.OVERDUE);
        } else if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        } else {
            goal.setStatus(GoalStatus.ACTIVE);
        }
        
        updateGoal(goal);
    }
    
    public BigDecimal getTotalTargetAmount() {
        String sql = "SELECT COALESCE(SUM(target_amount), 0) FROM goals WHERE status != 'COMPLETED'";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            
            return BigDecimal.ZERO;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get total target amount", e);
            throw new RuntimeException("Failed to get total target amount", e);
        }
    }
    
    public BigDecimal getTotalCurrentAmount() {
        String sql = "SELECT COALESCE(SUM(current_amount), 0) FROM goals";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            
            return BigDecimal.ZERO;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get total current amount", e);
            throw new RuntimeException("Failed to get total current amount", e);
        }
    }
    
    public BigDecimal getOverallProgress() {
        BigDecimal totalTarget = getTotalTargetAmount();
        if (totalTarget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalCurrent = getTotalCurrentAmount();
        return totalCurrent.divide(totalTarget, 4, BigDecimal.ROUND_HALF_UP)
                          .multiply(new BigDecimal("100"));
    }
    
    public List<Goal> getGoalsByPriority(Priority priority) {
        if (priority == null) {
            return getAllGoals();
        }
        
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE priority = ? ORDER BY created_date DESC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, priority.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get goals by priority: " + priority, e);
            throw new RuntimeException("Failed to get goals by priority", e);
        }
    }
    
    public List<Goal> getOverdueGoals() {
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals 
            WHERE target_date < ? AND status != 'COMPLETED'
            ORDER BY target_date ASC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Goal goal = buildGoalFromResultSet(rs);
                // Ensure status is set to overdue
                goal.setStatus(GoalStatus.OVERDUE);
                goals.add(goal);
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get overdue goals", e);
            throw new RuntimeException("Failed to get overdue goals", e);
        }
    }
    
    public int getGoalsCount() {
        String sql = "SELECT COUNT(*) FROM goals";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get goals count", e);
            throw new RuntimeException("Failed to get goals count", e);
        }
    }
    
    public int getActiveGoalsCount() {
        String sql = "SELECT COUNT(*) FROM goals WHERE status = 'ACTIVE'";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get active goals count", e);
            throw new RuntimeException("Failed to get active goals count", e);
        }
    }
    
    public int getCompletedGoalsCount() {
        String sql = "SELECT COUNT(*) FROM goals WHERE status = 'COMPLETED'";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get completed goals count", e);
            throw new RuntimeException("Failed to get completed goals count", e);
        }
    }
    
    private Goal buildGoalFromResultSet(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getString("id"));
        goal.setName(rs.getString("name"));
        goal.setDescription(rs.getString("description"));
        
        String typeStr = rs.getString("type");
        if (typeStr != null) {
            goal.setType(GoalType.valueOf(typeStr));
        }
        
        goal.setTargetAmount(rs.getBigDecimal("target_amount"));
        goal.setCurrentAmount(rs.getBigDecimal("current_amount"));
        
        Date targetDate = rs.getDate("target_date");
        if (targetDate != null) {
            goal.setTargetDate(targetDate.toLocalDate());
        }
        
        Date createdDate = rs.getDate("created_date");
        if (createdDate != null) {
            goal.setCreatedDate(createdDate.toLocalDate());
        }
        
        String priorityStr = rs.getString("priority");
        if (priorityStr != null) {
            goal.setPriority(Priority.valueOf(priorityStr));
        }
        
        goal.setMonthlyContribution(rs.getBigDecimal("monthly_contribution"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            goal.setStatus(GoalStatus.valueOf(statusStr));
        }
        
        return goal;
    }
    
    @Override
    public void updateProgress(String goalId, BigDecimal newAmount) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new RuntimeException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        goal.setCurrentAmount(newAmount);
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        updateGoal(goal);
    }
    
    @Override
    public void completeGoal(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new RuntimeException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        goal.setStatus(GoalStatus.COMPLETED);
        goal.setCurrentAmount(goal.getTargetAmount());
        
        updateGoal(goal);
    }
    
    // Stub implementations for unimplemented interface methods
    @Override public BigDecimal getProgressPercentage(String goalId) { return BigDecimal.ZERO; }
    @Override public BigDecimal getRemainingAmount(String goalId) { return BigDecimal.ZERO; }
    @Override public long getDaysRemaining(String goalId) { return 0; }
    @Override public BigDecimal getRequiredMonthlyContribution(String goalId) { return BigDecimal.ZERO; }
    @Override public boolean isGoalOnTrack(String goalId) { return true; }
    @Override public LocalDate getProjectedCompletionDate(String goalId) { return LocalDate.now(); }
    @Override public List<Goal> getGoalsAtRisk(String planId) { return new ArrayList<>(); }
    @Override public List<Goal> getCompletedGoals(String planId) { return getGoalsByStatus(GoalStatus.COMPLETED); }
    @Override public void evaluateGoal(String goalId) { }
    @Override public void evaluateAllGoals(String planId) { }
    @Override public List<String> getGoalRecommendations(String goalId) { return new ArrayList<>(); }
    @Override public Goal suggestGoalAdjustment(String goalId) { return getGoalById(goalId).orElse(null); }
    @Override public void setPriority(String goalId, Priority priority) { }
    @Override public List<Goal> getGoalsByPriority(String planId, Priority priority) { return new ArrayList<>(); }
    @Override public List<Goal> getSortedGoalsByPriority(String planId) { return new ArrayList<>(); }
    @Override public void reorderGoalPriorities(List<String> goalIds) { }
    @Override public List<Goal> suggestGoalsBasedOnProfile(String planId, String userId) { return new ArrayList<>(); }
    @Override public Goal createGoalFromTemplate(String templateName, String planId) { return new Goal(); }
    @Override public List<String> getAvailableGoalTemplates() { return new ArrayList<>(); }
    @Override public void saveGoalAsTemplate(String goalId, String templateName) { }
    @Override public void linkGoalToTransactions(String goalId, List<String> transactionIds) { }
    @Override public List<String> getGoalLinkedTransactions(String goalId) { return new ArrayList<>(); }
    @Override public void autoTrackGoalProgress(String goalId) { }
    @Override public BigDecimal calculateActualContributions(String goalId, LocalDate startDate, LocalDate endDate) { return BigDecimal.ZERO; }
    @Override public void addMilestone(String goalId, String description, BigDecimal amount, LocalDate date) { }
    @Override public List<Object> getGoalMilestones(String goalId) { return new ArrayList<>(); }
    @Override public boolean isMilestoneReached(String goalId, String milestoneId) { return false; }
    @Override public void markMilestoneComplete(String goalId, String milestoneId) { }
    @Override public void bulkUpdateGoals(List<Goal> goals) { }
    @Override public void archiveCompletedGoals(String planId) { }
    @Override public BigDecimal getTotalGoalTargets(String planId) { return BigDecimal.ZERO; }
    @Override public BigDecimal getTotalGoalProgress(String planId) { return BigDecimal.ZERO; }
    @Override public BigDecimal getOverallGoalProgress(String planId) { return BigDecimal.ZERO; }
    @Override public List<Goal> getGoalPerformanceSummary(String planId) { return new ArrayList<>(); }
    @Override public void syncGoalWithBudget(String goalId, String budgetId) { }
    @Override public void createBudgetForGoal(String goalId) { }
    @Override public void adjustGoalBasedOnBudgetChanges(String goalId) { }
}