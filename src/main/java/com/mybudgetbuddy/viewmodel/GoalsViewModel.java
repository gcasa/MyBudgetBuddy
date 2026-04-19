package com.mybudgetbuddy.viewmodel;

import com.mybudgetbuddy.application.service.GoalService;
import com.mybudgetbuddy.command.Command;
import com.mybudgetbuddy.command.RelayCommand;
import com.mybudgetbuddy.domain.model.Goal;
import com.mybudgetbuddy.domain.model.GoalStatus;
import com.mybudgetbuddy.domain.model.GoalType;
import com.mybudgetbuddy.domain.model.Priority;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Goals functionality with data binding and commands
 */
public class GoalsViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(GoalsViewModel.class.getName());
    
    // Service dependencies
    private final GoalService goalService;
    
    // Observable properties for data binding
    private final ObservableList<Goal> goalsList = FXCollections.observableArrayList();
    private final ObservableList<GoalType> availableGoalTypes = FXCollections.observableArrayList(GoalType.values());
    private final ObservableList<Priority> availablePriorities = FXCollections.observableArrayList(Priority.values());
    private final ObservableList<GoalStatus> availableStatuses = FXCollections.observableArrayList(GoalStatus.values());
    
    // Form properties
    private final StringProperty goalName = new SimpleStringProperty("");
    private final StringProperty goalDescription = new SimpleStringProperty("");
    private final ObjectProperty<GoalType> selectedGoalType = new SimpleObjectProperty<>(GoalType.SAVINGS);
    private final StringProperty targetAmountText = new SimpleStringProperty("0.00");
    private final StringProperty currentAmountText = new SimpleStringProperty("0.00");
    private final ObjectProperty<LocalDate> targetDate = new SimpleObjectProperty<>(LocalDate.now().plusMonths(12));
    private final ObjectProperty<Priority> selectedPriority = new SimpleObjectProperty<>(Priority.MEDIUM);
    private final StringProperty monthlyContributionText = new SimpleStringProperty("0.00");
    
    // Selected goal and editing mode
    private final ObjectProperty<Goal> selectedGoal = new SimpleObjectProperty<>();
    private final BooleanProperty isEditing = new SimpleBooleanProperty(false);
    private final BooleanProperty isCreatingNew = new SimpleBooleanProperty(false);
    
    // Filter properties
    private final ObjectProperty<GoalType> filterType = new SimpleObjectProperty<>();
    private final ObjectProperty<GoalStatus> filterStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<Priority> filterPriority = new SimpleObjectProperty<>();
    private final BooleanProperty showCompletedGoals = new SimpleBooleanProperty(true);
    
    // Progress and status properties
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");
    
    // Statistics properties
    private final StringProperty totalGoals = new SimpleStringProperty("0");
    private final StringProperty activeGoals = new SimpleStringProperty("0");
    private final StringProperty completedGoals = new SimpleStringProperty("0");
    private final StringProperty totalTargetAmount = new SimpleStringProperty("$0.00");
    private final StringProperty totalCurrentAmount = new SimpleStringProperty("$0.00");
    private final StringProperty overallProgress = new SimpleStringProperty("0%");
    
    // Plan context
    private final StringProperty planId = new SimpleStringProperty(null);
    
    // Commands
    private final Command createGoalCommand;
    private final Command editGoalCommand;
    private final Command saveGoalCommand;
    private final Command cancelEditCommand;
    private final Command deleteGoalCommand;
    private final Command refreshCommand;
    private final Command addContributionCommand;
    private final Command markCompleteCommand;
    private final Command pauseGoalCommand;
    private final Command resumeGoalCommand;
    private final Command duplicateGoalCommand;
    
    // Callbacks
    private Consumer<Goal> onEditGoal;
    private Consumer<String> onShowMessage;
    private Consumer<String> onShowError;
    
    public GoalsViewModel(GoalService goalService) {
        this.goalService = goalService;
        
        // Initialize commands
        this.createGoalCommand = new RelayCommand(this::handleCreateGoal, this::canCreateGoal);
        this.editGoalCommand = new RelayCommand(this::handleEditGoal, this::canEditGoal);
        this.saveGoalCommand = new RelayCommand(this::handleSaveGoal, this::canSaveGoal);
        this.cancelEditCommand = new RelayCommand(this::handleCancelEdit, this::canCancelEdit);
        this.deleteGoalCommand = new RelayCommand(this::handleDeleteGoal, this::canDeleteGoal);
        this.refreshCommand = new RelayCommand(this::handleRefresh, () -> !isLoading.get());
        this.addContributionCommand = new RelayCommand(this::handleAddContribution, this::canAddContribution);
        this.markCompleteCommand = new RelayCommand(this::handleMarkComplete, this::canMarkComplete);
        this.pauseGoalCommand = new RelayCommand(this::handlePauseGoal, this::canPauseGoal);
        this.resumeGoalCommand = new RelayCommand(this::handleResumeGoal, this::canResumeGoal);
        this.duplicateGoalCommand = new RelayCommand(this::handleDuplicateGoal, this::canDuplicateGoal);
        
        // Initialize data
        loadGoalsAsync();
    }
    
    // Command implementations
    private void handleCreateGoal() {
        clearForm();
        isCreatingNew.set(true);
        isEditing.set(true);
    }
    
    private void handleEditGoal() {
        Goal goal = selectedGoal.get();
        if (goal != null) {
            populateForm(goal);
            isCreatingNew.set(false);
            isEditing.set(true);
        }
    }
    
    private void handleSaveGoal() {
        try {
            Goal goal = createGoalFromForm();
            if (isCreatingNew.get()) {
                goalService.createGoal(goal);
                statusMessage.set("Goal created successfully");
            } else {
                goalService.updateGoal(goal);
                statusMessage.set("Goal updated successfully");
            }
            
            isEditing.set(false);
            isCreatingNew.set(false);
            clearForm();
            loadGoalsAsync();
            
            if (onShowMessage != null) {
                onShowMessage.accept("Goal saved successfully");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save goal", e);
            errorMessage.set("Failed to save goal: " + e.getMessage());
            if (onShowError != null) {
                onShowError.accept("Failed to save goal: " + e.getMessage());
            }
        }
    }
    
    private void handleCancelEdit() {
        isEditing.set(false);
        isCreatingNew.set(false);
        clearForm();
    }
    
    private void handleDeleteGoal() {
        Goal goal = selectedGoal.get();
        if (goal != null) {
            try {
                goalService.deleteGoal(goal.getId());
                statusMessage.set("Goal deleted successfully");
                loadGoalsAsync();
                
                if (onShowMessage != null) {
                    onShowMessage.accept("Goal deleted successfully");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete goal", e);
                errorMessage.set("Failed to delete goal: " + e.getMessage());
                if (onShowError != null) {
                    onShowError.accept("Failed to delete goal: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleRefresh() {
        loadGoalsAsync();
    }
    
    private void handleAddContribution() {
        Goal goal = selectedGoal.get();
        if (goal != null && !currentAmountText.get().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(currentAmountText.get());
                goalService.addContribution(goal.getId(), amount);
                statusMessage.set("Contribution added successfully");
                loadGoalsAsync();
                
                if (onShowMessage != null) {
                    onShowMessage.accept("Contribution added successfully");
                }
                
            } catch (NumberFormatException e) {
                errorMessage.set("Invalid contribution amount");
                if (onShowError != null) {
                    onShowError.accept("Invalid contribution amount");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to add contribution", e);
                errorMessage.set("Failed to add contribution: " + e.getMessage());
            }
        }
    }
    
    private void handleMarkComplete() {
        Goal goal = selectedGoal.get();
        if (goal != null) {
            try {
                goalService.updateProgress(goal.getId(), goal.getTargetAmount());
                statusMessage.set("Goal marked as complete");
                loadGoalsAsync();
                
                if (onShowMessage != null) {
                    onShowMessage.accept("Goal completed!");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to mark goal complete", e);
                errorMessage.set("Failed to mark goal complete: " + e.getMessage());
            }
        }
    }
    
    private void handlePauseGoal() {
        Goal goal = selectedGoal.get();
        if (goal != null) {
            try {
                goal.setStatus(GoalStatus.PAUSED);
                goalService.updateGoal(goal);
                statusMessage.set("Goal paused");
                loadGoalsAsync();
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to pause goal", e);
                errorMessage.set("Failed to pause goal: " + e.getMessage());
            }
        }
    }
    
    private void handleResumeGoal() {
        Goal goal = selectedGoal.get();
        if (goal != null) {
            try {
                goal.setStatus(GoalStatus.ACTIVE);
                goalService.updateGoal(goal);
                statusMessage.set("Goal resumed");
                loadGoalsAsync();
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to resume goal", e);
                errorMessage.set("Failed to resume goal: " + e.getMessage());
            }
        }
    }
    
    private void handleDuplicateGoal() {
        Goal original = selectedGoal.get();
        if (original != null) {
            Goal duplicate = new Goal(original.getName() + " (Copy)", 
                                    original.getType(), 
                                    original.getTargetAmount(), 
                                    original.getTargetDate());
            duplicate.setDescription(original.getDescription());
            duplicate.setPriority(original.getPriority());
            duplicate.setPlanId(original.getPlanId());
            
            try {
                goalService.createGoal(duplicate);
                statusMessage.set("Goal duplicated successfully");
                loadGoalsAsync();
                
                if (onShowMessage != null) {
                    onShowMessage.accept("Goal duplicated successfully");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to duplicate goal", e);
                errorMessage.set("Failed to duplicate goal: " + e.getMessage());
            }
        }
    }
    
    // Command can-execute predicates  
    private boolean canCreateGoal() {
        return !isLoading.get();
    }
    
    private boolean canEditGoal() {
        return selectedGoal.get() != null && !isEditing.get() && !isLoading.get();
    }
    
    private boolean canSaveGoal() {
        return isEditing.get() && !goalName.get().trim().isEmpty() && 
               !targetAmountText.get().trim().isEmpty() && !isLoading.get();
    }
    
    private boolean canCancelEdit() {
        return isEditing.get();
    }
    
    private boolean canDeleteGoal() {
        return selectedGoal.get() != null && !isEditing.get() && !isLoading.get();
    }
    
    private boolean canAddContribution() {
        Goal goal = selectedGoal.get();
        return goal != null && goal.getStatus() == GoalStatus.ACTIVE && 
               !currentAmountText.get().trim().isEmpty() && !isLoading.get();
    }
    
    private boolean canMarkComplete() {
        Goal goal = selectedGoal.get();
        return goal != null && goal.getStatus() == GoalStatus.ACTIVE && !isLoading.get();
    }
    
    private boolean canPauseGoal() {
        Goal goal = selectedGoal.get();
        return goal != null && goal.getStatus() == GoalStatus.ACTIVE && !isLoading.get();
    }
    
    private boolean canResumeGoal() {
        Goal goal = selectedGoal.get();
        return goal != null && goal.getStatus() == GoalStatus.PAUSED && !isLoading.get();
    }
    
    private boolean canDuplicateGoal() {
        return selectedGoal.get() != null && !isEditing.get() && !isLoading.get();
    }
    
    // Async operations
    private void loadGoalsAsync() {
        Task<List<Goal>> task = new Task<>() {
            @Override
            protected List<Goal> call() throws Exception {
                // GoalService handles null planId by returning all goals
                return goalService.getGoalsByPlanId(planId.get());
            }
            
            @Override
            protected void succeeded() {
                List<Goal> goals = getValue();
                goalsList.setAll(goals);
                updateStatistics();
                isLoading.set(false);
                statusMessage.set("Goals loaded successfully");
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                LOGGER.log(Level.SEVERE, "Failed to load goals", exception);
                errorMessage.set("Failed to load goals: " + exception.getMessage());
                isLoading.set(false);
            }
        };
        
        isLoading.set(true);
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
    
    // Helper methods
    private void clearForm() {
        goalName.set("New Goal");  // Set default name instead of empty string
        goalDescription.set("");
        selectedGoalType.set(GoalType.SAVINGS);
        targetAmountText.set("100.00");  // Set default target amount
        currentAmountText.set("0.00");
        targetDate.set(LocalDate.now().plusMonths(12));
        selectedPriority.set(Priority.MEDIUM);
        monthlyContributionText.set("0.00");
    }
    
    private void populateForm(Goal goal) {
        goalName.set(goal.getName());
        goalDescription.set(goal.getDescription() != null ? goal.getDescription() : "");
        selectedGoalType.set(goal.getType());
        targetAmountText.set(goal.getTargetAmount().toString());
        currentAmountText.set(goal.getCurrentAmount().toString());
        targetDate.set(goal.getTargetDate());
        selectedPriority.set(goal.getPriority());
        monthlyContributionText.set(goal.getMonthlyContribution() != null ? 
                                   goal.getMonthlyContribution().toString() : "0.00");
    }
    
    private Goal createGoalFromForm() {
        Goal goal;
        if (isCreatingNew.get()) {
            goal = new Goal();
        } else {
            goal = selectedGoal.get();
        }
        
        goal.setName(goalName.get());
        goal.setDescription(goalDescription.get());
        goal.setType(selectedGoalType.get());
        goal.setTargetAmount(new BigDecimal(targetAmountText.get()));
        goal.setCurrentAmount(new BigDecimal(currentAmountText.get()));
        goal.setTargetDate(targetDate.get());
        goal.setPriority(selectedPriority.get());
        goal.setPlanId(planId.get());
        goal.setLastUpdated(LocalDate.now()); // Ensure last_updated is set
        
        if (!monthlyContributionText.get().trim().isEmpty()) {
            goal.setMonthlyContribution(new BigDecimal(monthlyContributionText.get()));
        }
        
        return goal;
    }
    
    private void updateStatistics() {
        List<Goal> goals = goalsList;
        
        totalGoals.set(String.valueOf(goals.size()));
        
        long active = goals.stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).count();
        activeGoals.set(String.valueOf(active));
        
        long completed = goals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
        completedGoals.set(String.valueOf(completed));
        
        BigDecimal totalTarget = goals.stream()
            .map(Goal::getTargetAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalTargetAmount.set(String.format("$%,.2f", totalTarget));
        
        BigDecimal totalCurrent = goals.stream()
            .map(Goal::getCurrentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalCurrentAmount.set(String.format("$%,.2f", totalCurrent));
        
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = totalCurrent.divide(totalTarget, 2, java.math.RoundingMode.HALF_UP)
                                            .multiply(new BigDecimal("100"));
            overallProgress.set(String.format("%.1f%%", progress));
        } else {
            overallProgress.set("0%");
        }
    }
    
    // Property getters
    public ObservableList<Goal> getGoalsList() { return goalsList; }
    public ObservableList<GoalType> getAvailableGoalTypes() { return availableGoalTypes; }
    public ObservableList<Priority> getAvailablePriorities() { return availablePriorities; }
    public ObservableList<GoalStatus> getAvailableStatuses() { return availableStatuses; }
    
    public StringProperty goalNameProperty() { return goalName; }
    public StringProperty goalDescriptionProperty() { return goalDescription; }
    public ObjectProperty<GoalType> selectedGoalTypeProperty() { return selectedGoalType; }
    public StringProperty targetAmountTextProperty() { return targetAmountText; }
    public StringProperty currentAmountTextProperty() { return currentAmountText; }
    public ObjectProperty<LocalDate> targetDateProperty() { return targetDate; }
    public ObjectProperty<Priority> selectedPriorityProperty() { return selectedPriority; }
    public StringProperty monthlyContributionTextProperty() { return monthlyContributionText; }
    
    public ObjectProperty<Goal> selectedGoalProperty() { return selectedGoal; }
    public BooleanProperty isEditingProperty() { return isEditing; }
    public BooleanProperty isCreatingNewProperty() { return isCreatingNew; }
    
    public ObjectProperty<GoalType> filterTypeProperty() { return filterType; }
    public ObjectProperty<GoalStatus> filterStatusProperty() { return filterStatus; }
    public ObjectProperty<Priority> filterPriorityProperty() { return filterPriority; }
    public BooleanProperty showCompletedGoalsProperty() { return showCompletedGoals; }
    
    public BooleanProperty isLoadingProperty() { return isLoading; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public StringProperty errorMessageProperty() { return errorMessage; }
    
    public StringProperty totalGoalsProperty() { return totalGoals; }
    public StringProperty activeGoalsProperty() { return activeGoals; }
    public StringProperty completedGoalsProperty() { return completedGoals; }
    public StringProperty totalTargetAmountProperty() { return totalTargetAmount; }
    public StringProperty totalCurrentAmountProperty() { return totalCurrentAmount; }
    public StringProperty overallProgressProperty() { return overallProgress; }
    
    public StringProperty planIdProperty() { return planId; }
    
    // Command getters
    public Command getCreateGoalCommand() { return createGoalCommand; }
    public Command getEditGoalCommand() { return editGoalCommand; }
    public Command getSaveGoalCommand() { return saveGoalCommand; }
    public Command getCancelEditCommand() { return cancelEditCommand; }
    public Command getDeleteGoalCommand() { return deleteGoalCommand; }
    public Command getRefreshCommand() { return refreshCommand; }
    public Command getAddContributionCommand() { return addContributionCommand; }
    public Command getMarkCompleteCommand() { return markCompleteCommand; }
    public Command getPauseGoalCommand() { return pauseGoalCommand; }
    public Command getResumeGoalCommand() { return resumeGoalCommand; }
    public Command getDuplicateGoalCommand() { return duplicateGoalCommand; }
    
    // Callback setters
    public void setOnEditGoal(Consumer<Goal> onEditGoal) { this.onEditGoal = onEditGoal; }
    public void setOnShowMessage(Consumer<String> onShowMessage) { this.onShowMessage = onShowMessage; }
    public void setOnShowError(Consumer<String> onShowError) { this.onShowError = onShowError; }
}