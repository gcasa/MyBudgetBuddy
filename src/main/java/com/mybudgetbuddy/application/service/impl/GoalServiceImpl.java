package com.mybudgetbuddy.application.service.impl;

import com.mybudgetbuddy.application.service.GoalService;
import com.mybudgetbuddy.domain.model.*;
import com.mybudgetbuddy.infrastructure.database.DatabaseManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
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
                             target_date, created_date, priority, monthly_contribution, status, last_updated)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, goal.getId());
            stmt.setString(2, goal.getName());
            stmt.setString(3, goal.getDescription());
            stmt.setString(4, goal.getType() != null ? goal.getType().name() : null);
            stmt.setBigDecimal(5, goal.getTargetAmount());
            stmt.setBigDecimal(6, goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
            stmt.setDate(7, goal.getTargetDate() != null ? java.sql.Date.valueOf(goal.getTargetDate()) : null);
            stmt.setDate(8, goal.getCreatedDate() != null ? java.sql.Date.valueOf(goal.getCreatedDate()) : java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(9, goal.getPriority() != null ? goal.getPriority().name() : null);
            stmt.setBigDecimal(10, goal.getMonthlyContribution());
            stmt.setString(11, goal.getStatus() != null ? goal.getStatus().name() : GoalStatus.ACTIVE.name());
            stmt.setDate(12, goal.getLastUpdated() != null ? java.sql.Date.valueOf(goal.getLastUpdated()) : java.sql.Date.valueOf(LocalDate.now()));
            
            stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Created goal: {0}", goal.getName());
            
            return goal;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to create goal: " + goal.getName());
            throw new IllegalStateException("Failed to create goal", e);
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
            LOGGER.log(Level.SEVERE, () -> "Failed to get goal by ID: " + id);
            throw new IllegalStateException("Failed to get goal", e);
        }
    }
    
    @Override
    public List<Goal> getGoalsByPlanId(String planId) {
        if (planId == null || planId.trim().isEmpty()) {
            return getAllGoals(); // Return all goals if no specific plan
        }
        
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE plan_id = ? ORDER BY created_date DESC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            LOGGER.log(Level.INFO, "Retrieved {0} goals for plan: {1}", new Object[]{goals.size(), planId});
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to get goals by plan ID: " + planId);
            // Return all goals as fallback
            return getAllGoals();
        }
    }
    
    @Override
    public List<Goal> getActiveGoals(String planId) {
        if (planId == null || planId.trim().isEmpty()) {
            return getGoalsByStatus(GoalStatus.ACTIVE);
        }
        
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE plan_id = ? AND status = 'ACTIVE' ORDER BY priority DESC, target_date ASC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to get active goals by plan ID: " + planId);
            // Return all active goals as fallback
            return getGoalsByStatus(GoalStatus.ACTIVE);
        }
    }
    
    @Override
    public Goal updateGoal(Goal goal) {
        if (goal == null || goal.getId() == null) {
            throw new IllegalArgumentException("Goal and goal ID cannot be null");
        }
        
        String sql = """
            UPDATE goals SET name = ?, description = ?, type = ?, target_amount = ?, 
                           current_amount = ?, target_date = ?, priority = ?, 
                           monthly_contribution = ?, status = ?, last_updated = CURRENT_DATE
            WHERE id = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, goal.getName());
            stmt.setString(2, goal.getDescription());
            stmt.setString(3, goal.getType() != null ? goal.getType().name() : null);
            stmt.setBigDecimal(4, goal.getTargetAmount());
            stmt.setBigDecimal(5, goal.getCurrentAmount());
            stmt.setDate(6, goal.getTargetDate() != null ? java.sql.Date.valueOf(goal.getTargetDate()) : null);
            stmt.setString(7, goal.getPriority() != null ? goal.getPriority().name() : null);
            stmt.setBigDecimal(8, goal.getMonthlyContribution());
            stmt.setString(9, goal.getStatus() != null ? goal.getStatus().name() : null);
            stmt.setString(10, goal.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Goal not found for update: " + goal.getId());
            }
            
            LOGGER.log(Level.INFO, "Updated goal: {0}", goal.getName());
            return goal;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to update goal: " + goal.getId());
            throw new IllegalStateException("Failed to update goal", e);
        }
    }
    
    @Override
    public void deleteGoal(String goalId) {
        if (goalId == null || goalId.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal ID cannot be null or empty");
        }
        
        String sql = "DELETE FROM goals WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, goalId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Deleted goal: {0}", goalId);
            } else {
                LOGGER.log(Level.WARNING, "No goal found to delete: {0}", goalId);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to delete goal: " + goalId);
            throw new IllegalStateException("Failed to delete goal", e);
        }
    }
    
    @Override
    public void updateProgress(String goalId, BigDecimal newAmount) {
        if (goalId == null || newAmount == null) {
            throw new IllegalArgumentException("Goal ID and amount cannot be null");
        }
        
        String sql = """
            UPDATE goals SET current_amount = ?, 
                           status = CASE 
                               WHEN current_amount >= target_amount THEN 'COMPLETED'
                               WHEN target_date < CURRENT_DATE AND current_amount < target_amount THEN 'OVERDUE'
                               ELSE status 
                           END,
                           last_updated = CURRENT_DATE
            WHERE id = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, newAmount);
            stmt.setString(2, goalId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Goal not found for progress update: " + goalId);
            }
            
            LOGGER.log(Level.INFO, "Updated progress for goal: {0} to amount: {1}", 
                      new Object[]{goalId, newAmount});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to update progress for goal: " + goalId);
            throw new IllegalStateException("Failed to update progress", e);
        }
    }
    
    @Override
    public void addContribution(String goalId, BigDecimal contributionAmount) {
        if (goalId == null || contributionAmount == null) {
            throw new IllegalArgumentException("Goal ID and contribution amount cannot be null");
        }
        
        String sql = """
            UPDATE goals SET current_amount = current_amount + ?, 
                           status = CASE 
                               WHEN (current_amount + ?) >= target_amount THEN 'COMPLETED'
                               WHEN target_date < CURRENT_DATE AND (current_amount + ?) < target_amount THEN 'OVERDUE'
                               ELSE status 
                           END,
                           last_updated = CURRENT_DATE
            WHERE id = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, contributionAmount);
            stmt.setBigDecimal(2, contributionAmount);
            stmt.setBigDecimal(3, contributionAmount); 
            stmt.setString(4, goalId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Goal not found for contribution: " + goalId);
            }
            
            LOGGER.log(Level.INFO, "Added contribution for goal: {0} amount: {1}", 
                      new Object[]{goalId, contributionAmount});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to add contribution for goal: " + goalId);
            throw new IllegalStateException("Failed to add contribution", e);
        }
    }
    
    @Override
    public BigDecimal getProgressPercentage(String goalId) {
        Optional<Goal> goal = getGoalById(goalId);
        return goal.map(Goal::getProgressPercentage).orElse(BigDecimal.ZERO);
    }
    
    @Override
    public BigDecimal getRemainingAmount(String goalId) {
        Optional<Goal> goal = getGoalById(goalId);
        return goal.map(Goal::getRemainingAmount).orElse(BigDecimal.ZERO);
    }
    
    @Override
    public long getDaysRemaining(String goalId) {
        Optional<Goal> goal = getGoalById(goalId);
        return goal.map(Goal::getDaysRemaining).orElse(0L);
    }
    
    @Override
    public BigDecimal getRequiredMonthlyContribution(String goalId) {
        Optional<Goal> goal = getGoalById(goalId);
        return goal.map(Goal::getRequiredMonthlyContribution).orElse(BigDecimal.ZERO);
    }
    
    @Override
    public boolean isGoalOnTrack(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) return false;
        
        Goal goal = goalOpt.get();
        if (goal.getTargetDate() == null) return true; // No timeline, always on track
        
        BigDecimal requiredContribution = goal.getRequiredMonthlyContribution();
        BigDecimal actualContribution = goal.getMonthlyContribution();
        
        return actualContribution != null && actualContribution.compareTo(requiredContribution) >= 0;
    }
    
    @Override
    public LocalDate getProjectedCompletionDate(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) return null;
        
        Goal goal = goalOpt.get();
        if (goal.getMonthlyContribution() == null || 
            goal.getMonthlyContribution().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        BigDecimal remainingAmount = goal.getRemainingAmount();
        BigDecimal monthlyContribution = goal.getMonthlyContribution();
        
        long monthsToComplete = remainingAmount.divide(monthlyContribution, 0, RoundingMode.UP).longValue();
        return LocalDate.now().plusMonths(monthsToComplete);
    }
    
    @Override
    public List<Goal> getGoalsAtRisk(String planId) {
        List<Goal> allGoals = getActiveGoals(planId);
        return allGoals.stream()
            .filter(goal -> !isGoalOnTrack(goal.getId()))
            .toList();
    }
    
    @Override
    public List<Goal> getCompletedGoals(String planId) {
        if (planId == null || planId.trim().isEmpty()) {
            return getGoalsByStatus(GoalStatus.COMPLETED);
        }
        
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE plan_id = ? AND status = 'COMPLETED' ORDER BY last_updated DESC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to get completed goals by plan ID: " + planId);
            return getGoalsByStatus(GoalStatus.COMPLETED);
        }
    }
    
    @Override
    public void evaluateGoal(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) return;
        
        Goal goal = goalOpt.get();
        
        // Update goal status based on current conditions
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        } else if (goal.getTargetDate() != null && goal.getTargetDate().isBefore(LocalDate.now())) {
            goal.setStatus(GoalStatus.OVERDUE);
        } else if (!isGoalOnTrack(goalId)) {
            goal.setStatus(GoalStatus.OVERDUE);
        } else {
            goal.setStatus(GoalStatus.ACTIVE);
        }
        
        updateGoal(goal);
        LOGGER.log(Level.INFO, "Evaluated goal: {0} - Status: {1}", new Object[]{goalId, goal.getStatus()});
    }
    
    @Override
    public void evaluateAllGoals(String planId) {
        List<Goal> goals = getGoalsByPlanId(planId);
        for (Goal goal : goals) {
            evaluateGoal(goal.getId());
        }
        LOGGER.log(Level.INFO, "Evaluated {0} goals for plan: {1}", new Object[]{goals.size(), planId});
    }
    
    @Override
    public List<String> getGoalRecommendations(String goalId) {
        List<String> recommendations = new ArrayList<>();
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) return recommendations;
        
        Goal goal = goalOpt.get();
        
        // Analyze goal and provide recommendations
        if (!isGoalOnTrack(goalId)) {
            recommendations.add("Consider increasing your monthly contribution to stay on track");
            recommendations.add("Review your spending to free up funds for this goal");
        }
        
        if (goal.getMonthlyContribution() == null || goal.getMonthlyContribution().compareTo(BigDecimal.ZERO) == 0) {
            recommendations.add("Set up automatic monthly contributions for consistent progress");
        }
        
        BigDecimal required = getRequiredMonthlyContribution(goalId);
        if (goal.getMonthlyContribution() != null && goal.getMonthlyContribution().compareTo(required) < 0) {
            recommendations.add("Increase monthly contribution to $" + required + " to meet your target date");
        }
        
        if (goal.getTargetDate() != null && goal.getTargetDate().isBefore(LocalDate.now().plusDays(30))) {
            recommendations.add("Target date is approaching - consider adjusting deadline or increasing contributions");
        }
        
        return recommendations;
    }
    
    @Override
    public Goal suggestGoalAdjustment(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) return null;
        
        Goal goal = goalOpt.get();
        Goal adjustedGoal = new Goal();
        
        // Copy all properties
        adjustedGoal.setId(goal.getId());
        adjustedGoal.setName(goal.getName());
        adjustedGoal.setDescription(goal.getDescription());
        adjustedGoal.setType(goal.getType());
        adjustedGoal.setTargetAmount(goal.getTargetAmount());
        adjustedGoal.setCurrentAmount(goal.getCurrentAmount());
        adjustedGoal.setCreatedDate(goal.getCreatedDate());
        adjustedGoal.setPriority(goal.getPriority());
        adjustedGoal.setStatus(goal.getStatus());
        
        // Suggest adjustments based on current progress
        if (!isGoalOnTrack(goalId)) {
            // Suggest realistic new target date based on current contribution rate
            LocalDate projectedDate = getProjectedCompletionDate(goalId);
            adjustedGoal.setTargetDate(projectedDate);
            
            // Or suggest required monthly contribution for original date
            BigDecimal requiredContribution = getRequiredMonthlyContribution(goalId);
            adjustedGoal.setMonthlyContribution(requiredContribution);
        } else {
            // Keep original values if on track
            adjustedGoal.setTargetDate(goal.getTargetDate());
            adjustedGoal.setMonthlyContribution(goal.getMonthlyContribution());
        }
        
        return adjustedGoal;
    }
    
    @Override
    public void setPriority(String goalId, Priority priority) {
        String sql = "UPDATE goals SET priority = ? WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, priority.name());
            stmt.setString(2, goalId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Goal not found: " + goalId);
            }
            
            LOGGER.log(Level.INFO, "Updated priority for goal: {0} to: {1}", new Object[]{goalId, priority});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to set priority for goal: " + goalId);
            throw new IllegalStateException("Failed to set goal priority", e);
        }
    }
    
    @Override
    public List<Goal> getGoalsByPriority(String planId, Priority priority) {
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE plan_id = ? AND priority = ? ORDER BY created_date DESC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            stmt.setString(2, priority.name());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to get goals by priority: " + priority + " for plan: " + planId);
            return getGoalsByPriority(priority);
        }
    }
    
    @Override
    public List<Goal> getSortedGoalsByPriority(String planId) {
        String sql = """
            SELECT id, name, description, type, target_amount, current_amount, 
                   target_date, created_date, priority, monthly_contribution, status
            FROM goals WHERE plan_id = ? 
            ORDER BY 
                CASE priority 
                    WHEN 'HIGH' THEN 1 
                    WHEN 'MEDIUM' THEN 2 
                    WHEN 'LOW' THEN 3 
                    ELSE 4 
                END,
                created_date ASC
        """;
        
        List<Goal> goals = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                goals.add(buildGoalFromResultSet(rs));
            }
            
            return goals;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, () -> "Failed to get sorted goals by priority for plan: " + planId);
            return getAllGoals();
        }
    }
    
    @Override
    public void reorderGoalPriorities(List<String> goalIds) {
        // Assign priorities based on order: first = HIGH, middle = MEDIUM, last = LOW
        int totalGoals = goalIds.size();
        
        for (int i = 0; i < totalGoals; i++) {
            Priority priority;
            if (i < totalGoals / 3) {
                priority = Priority.HIGH;
            } else if (i < (2 * totalGoals) / 3) {
                priority = Priority.MEDIUM;
            } else {
                priority = Priority.LOW;
            }
            
            setPriority(goalIds.get(i), priority);
        }
        
        LOGGER.log(Level.INFO, "Reordered priorities for {0} goals", totalGoals);
    }
    
    // Template and suggestion methods (placeholder implementations)
    
    @Override
    public List<Goal> suggestGoalsBasedOnProfile(String planId, String userId) {
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    @Override
    public Goal createGoalFromTemplate(String templateName, String planId) {
        // Placeholder implementation
        return null;
    }
    
    @Override
    public List<String> getAvailableGoalTemplates() {
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    @Override
    public void saveGoalAsTemplate(String goalId, String templateName) {
        // Placeholder implementation
    }
    
    // Goal tracking methods (placeholder implementations)
    
    @Override
    public void linkGoalToTransactions(String goalId, List<String> transactionIds) {
        // Placeholder implementation
    }
    
    @Override
    public List<String> getGoalLinkedTransactions(String goalId) {
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    @Override
    public void autoTrackGoalProgress(String goalId) {
        // Placeholder implementation
    }
    
    @Override
    public BigDecimal calculateActualContributions(String goalId, LocalDate startDate, LocalDate endDate) {
        // Placeholder implementation
        return BigDecimal.ZERO;
    }
    
    // Milestone methods (placeholder implementations)
    
    @Override
    public void addMilestone(String goalId, String description, BigDecimal amount, LocalDate date) {
        // Placeholder implementation
    }
    
    @Override
    public List<Object> getGoalMilestones(String goalId) {
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    @Override
    public boolean isMilestoneReached(String goalId, String milestoneId) {
        // Placeholder implementation
        return false;
    }
    
    @Override
    public void markMilestoneComplete(String goalId, String milestoneId) {
        // Placeholder implementation
    }
    
    // Bulk operations
    
    @Override
    public void pauseGoal(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new IllegalArgumentException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        goal.setStatus(GoalStatus.PAUSED);
        
        updateGoal(goal);
    }
    
    @Override
    public void resumeGoal(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new IllegalArgumentException("Goal not found: " + goalId);
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
    
    @Override
    public void completeGoal(String goalId) {
        Optional<Goal> goalOpt = getGoalById(goalId);
        if (goalOpt.isEmpty()) {
            throw new IllegalArgumentException("Goal not found: " + goalId);
        }
        
        Goal goal = goalOpt.get();
        goal.setStatus(GoalStatus.COMPLETED);
        goal.setCurrentAmount(goal.getTargetAmount());
        
        updateGoal(goal);
    }
    
    @Override
    public void bulkUpdateGoals(List<Goal> goals) {
        for (Goal goal : goals) {
            updateGoal(goal);
        }
    }
    
    @Override
    public void archiveCompletedGoals(String planId) {
        // Placeholder implementation
    }
    
    // Reporting methods
    
    @Override
    public BigDecimal getTotalGoalTargets(String planId) {
        List<Goal> goals = getGoalsByPlanId(planId);
        return goals.stream()
            .map(Goal::getTargetAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal getTotalGoalProgress(String planId) {
        List<Goal> goals = getGoalsByPlanId(planId);
        return goals.stream()
            .map(Goal::getCurrentAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public BigDecimal getOverallGoalProgress(String planId) {
        BigDecimal totalTarget = getTotalGoalTargets(planId);
        if (totalTarget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalProgress = getTotalGoalProgress(planId);
        return totalProgress.divide(totalTarget, 4, RoundingMode.HALF_UP)
                          .multiply(new BigDecimal("100"));
    }
    
    @Override
    public List<Goal> getGoalPerformanceSummary(String planId) {
        return getGoalsByPlanId(planId);
    }
    
    // Integration methods (placeholder implementations)
    
    @Override
    public void syncGoalWithBudget(String goalId, String budgetId) {
        // Placeholder implementation
    }
    
    @Override
    public void createBudgetForGoal(String goalId) {
        // Placeholder implementation
    }
    
    @Override
    public void adjustGoalBasedOnBudgetChanges(String goalId) {
        // Placeholder implementation
    }
    
    // Helper methods
    
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
            throw new IllegalStateException("Failed to get goals", e);
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
            LOGGER.log(Level.SEVERE, () -> "Failed to get goals by status: " + status);
            throw new IllegalStateException("Failed to get goals by status", e);
        }
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
            LOGGER.log(Level.SEVERE, () -> "Failed to get goals by priority: " + priority);
            throw new IllegalStateException("Failed to get goals by priority", e);
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
        
        java.sql.Date targetDate = rs.getDate("target_date");
        if (targetDate != null) {
            goal.setTargetDate(targetDate.toLocalDate());
        }
        
        java.sql.Date createdDate = rs.getDate("created_date");
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
}