package com.mybudgetbuddy.presentation.viewmodel;

import com.mybudgetbuddy.application.service.BudgetService;
import com.mybudgetbuddy.domain.model.Budget;
import com.mybudgetbuddy.domain.model.BudgetType;
import com.mybudgetbuddy.command.Command;
import com.mybudgetbuddy.command.RelayCommand;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Consumer;

public class BudgetViewModel {
    
    // Services
    private final BudgetService budgetService;
    
    // Form properties for creating/editing budgets
    private final StringProperty budgetName;
    private final StringProperty budgetDescription;
    private final StringProperty allocatedAmount;
    private final ObjectProperty<String> selectedCategoryId;
    private final ObjectProperty<BudgetType> selectedBudgetType;
    private final ObjectProperty<Period> selectedPeriod;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;
    private final StringProperty warningThreshold;
    
    // Budget list and selection
    private final ObservableList<Budget> budgets;
    private final ObjectProperty<Budget> selectedBudget;
    private final ObservableList<Budget> overBudgets;
    private final ObservableList<Budget> nearThresholdBudgets;
    
    // Summary properties
    private final StringProperty totalAllocated;
    private final StringProperty totalSpent;
    private final StringProperty totalRemaining;
    private final StringProperty overallProgress;
    
    // Filter properties
    private final ObjectProperty<BudgetType> filterBudgetType;
    private final BooleanProperty showOnlyActive;
    private final BooleanProperty showOnlyOverBudget;
    private final StringProperty searchText;
    
    // Validation
    private final BooleanProperty isFormValid;
    private final StringProperty validationErrors;
    
    // Commands
    private final Command createBudgetCommand;
    private final Command updateBudgetCommand;
    private final Command deleteBudgetCommand;
    private final Command resetBudgetCommand;
    private final Command refreshDataCommand;
    private final Command exportBudgetsCommand;
    private final Command importBudgetsCommand;
    
    // Callbacks
    private Consumer<Budget> onBudgetCreated;
    private Consumer<Budget> onBudgetUpdated;
    private Consumer<String> onBudgetDeleted;

    
    // Context
    private String currentPlanId;
    private Budget currentEditingBudget;
    
    public BudgetViewModel(BudgetService budgetService) {
        this.budgetService = budgetService;
        
        // Initialize form properties
        this.budgetName = new SimpleStringProperty("");
        this.budgetDescription = new SimpleStringProperty("");
        this.allocatedAmount = new SimpleStringProperty("0.00");
        this.selectedCategoryId = new SimpleObjectProperty<>();
        this.selectedBudgetType = new SimpleObjectProperty<>(BudgetType.CATEGORY_BUDGET);
        this.selectedPeriod = new SimpleObjectProperty<>(Period.ofMonths(1));
        this.startDate = new SimpleObjectProperty<>(LocalDate.now().withDayOfMonth(1));
        this.endDate = new SimpleObjectProperty<>(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        this.warningThreshold = new SimpleStringProperty("80");
        
        // Initialize collections
        this.budgets = FXCollections.observableArrayList();
        this.selectedBudget = new SimpleObjectProperty<>();
        this.overBudgets = FXCollections.observableArrayList();
        this.nearThresholdBudgets = FXCollections.observableArrayList();
        
        // Initialize summary properties
        this.totalAllocated = new SimpleStringProperty("$0.00");
        this.totalSpent = new SimpleStringProperty("$0.00");
        this.totalRemaining = new SimpleStringProperty("$0.00");
        this.overallProgress = new SimpleStringProperty("0%");
        
        // Initialize filter properties
        this.filterBudgetType = new SimpleObjectProperty<>();
        this.showOnlyActive = new SimpleBooleanProperty(true);
        this.showOnlyOverBudget = new SimpleBooleanProperty(false);
        this.searchText = new SimpleStringProperty("");
        
        // Initialize validation
        this.validationErrors = new SimpleStringProperty("");
        this.isFormValid = new SimpleBooleanProperty(false);
        
        // Initialize commands
        this.createBudgetCommand = new RelayCommand(this::handleCreateBudget, this::canCreateBudget);
        this.updateBudgetCommand = new RelayCommand(this::handleUpdateBudget, this::canUpdateBudget);
        this.deleteBudgetCommand = new RelayCommand(this::handleDeleteBudget, this::canDeleteBudget);
        this.resetBudgetCommand = new RelayCommand(this::handleResetBudget);
        this.refreshDataCommand = new RelayCommand(this::refreshData);
        this.exportBudgetsCommand = new RelayCommand(this::handleExportBudgets);
        this.importBudgetsCommand = new RelayCommand(this::handleImportBudgets);
        
        // Setup validation
        setupValidation();
        
        // Setup filter listening
        setupFilterListeners();
    }
    
    // Public methods
    public void loadBudgets(String planId) {
        this.currentPlanId = planId;
        refreshData();
    }
    
    public void editBudget(Budget budget) {
        this.currentEditingBudget = budget;
        populateFormFromBudget(budget);
    }
    
    public void clearForm() {
        this.currentEditingBudget = null;
        budgetName.set("");
        budgetDescription.set("");
        allocatedAmount.set("0.00");
        selectedCategoryId.set(null);
        selectedBudgetType.set(BudgetType.CATEGORY_BUDGET);
        selectedPeriod.set(Period.ofMonths(1));
        startDate.set(LocalDate.now().withDayOfMonth(1));
        endDate.set(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        warningThreshold.set("80");
    }
    
    private void refreshData() {
        if (currentPlanId == null) return;
        
        List<Budget> allBudgets = budgetService.getBudgetsByPlanId(currentPlanId);
        budgets.setAll(allBudgets);
        
        updateSummaryData();
        updateAlertBudgets();
        applyFilters();
    }
    
    private void populateFormFromBudget(Budget budget) {
        budgetName.set(budget.getName());
        budgetDescription.set(budget.getName()); // Assuming description field
        allocatedAmount.set(budget.getAllocatedAmount().toString());
        selectedCategoryId.set(budget.getCategoryId());
        selectedBudgetType.set(budget.getType());
        selectedPeriod.set(budget.getBudgetPeriod());
        startDate.set(budget.getStartDate());
        endDate.set(budget.getEndDate());
        warningThreshold.set(budget.getWarningThreshold().multiply(new BigDecimal("100")).toString());
    }
    
    private void updateSummaryData() {
        BigDecimal totalAllocatedAmount = budgets.stream()
                .map(Budget::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSpentAmount = budgets.stream()
                .map(Budget::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRemainingAmount = totalAllocatedAmount.subtract(totalSpentAmount);
        
        totalAllocated.set(String.format("$%,.2f", totalAllocatedAmount));
        totalSpent.set(String.format("$%,.2f", totalSpentAmount));
        totalRemaining.set(String.format("$%,.2f", totalRemainingAmount));
        
        if (totalAllocatedAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progressPercent = totalSpentAmount.divide(totalAllocatedAmount, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            overallProgress.set(String.format("%.1f%%", progressPercent));
        } else {
            overallProgress.set("0%");
        }
    }
    
    private void updateAlertBudgets() {
        overBudgets.setAll(budgets.stream().filter(Budget::isOverBudget).toList());
        nearThresholdBudgets.setAll(budgets.stream().filter(Budget::isNearWarningThreshold).toList());
    }
    
    private void applyFilters() {
        // TODO: Implement filtering logic based on filter properties
    }
    
    private void setupValidation() {
        // TODO: Add validation logic for form fields
        isFormValid.set(true); // Placeholder
    }
    
    private void setupFilterListeners() {
        // TODO: Add listeners for filter properties to trigger re-filtering
    }
    
    // Command handlers
    private void handleCreateBudget() {
        try {
            Budget budget = createBudgetFromForm();
            Budget created = budgetService.createBudget(budget);
            
            if (onBudgetCreated != null) {
                onBudgetCreated.accept(created);
            }
            
            clearForm();
            refreshData();
        } catch (Exception e) {
            validationErrors.set("Error creating budget: " + e.getMessage());
        }
    }
    
    private void handleUpdateBudget() {
        if (currentEditingBudget == null) return;
        
        try {
            updateBudgetFromForm(currentEditingBudget);
            Budget updated = budgetService.updateBudget(currentEditingBudget);
            
            if (onBudgetUpdated != null) {
                onBudgetUpdated.accept(updated);
            }
            
            clearForm();
            refreshData();
        } catch (Exception e) {
            validationErrors.set("Error updating budget: " + e.getMessage());
        }
    }
    
    private void handleDeleteBudget() {
        Budget selected = selectedBudget.get();
        if (selected == null) return;
        
        try {
            budgetService.deleteBudget(selected.getId());
            
            if (onBudgetDeleted != null) {
                onBudgetDeleted.accept(selected.getId());
            }
            
            refreshData();
        } catch (Exception e) {
            validationErrors.set("Error deleting budget: " + e.getMessage());
        }
    }
    
    private void handleResetBudget() {
        Budget selected = selectedBudget.get();
        if (selected == null) return;
        // TODO: Implement budget reset logic
    }
    
    private void handleExportBudgets() {
        // TODO: Implement budget export
    }
    
    private void handleImportBudgets() {
        // TODO: Implement budget import
    }
    
    private Budget createBudgetFromForm() {
        Budget budget = new Budget();
        budget.setPlanId(currentPlanId);
        updateBudgetFromForm(budget);
        return budget;
    }
    
    private void updateBudgetFromForm(Budget budget) {
        budget.setName(budgetName.get());
        budget.setAllocatedAmount(new BigDecimal(allocatedAmount.get()));
        budget.setCategoryId(selectedCategoryId.get());
        budget.setType(selectedBudgetType.get());
        budget.setBudgetPeriod(selectedPeriod.get());
        budget.setStartDate(startDate.get());
        budget.setEndDate(endDate.get());
        budget.setWarningThreshold(new BigDecimal(warningThreshold.get()).divide(new BigDecimal("100")));
    }
    
    // Validation methods
    private boolean canCreateBudget() {
        return isFormValid.get() && currentEditingBudget == null;
    }
    
    private boolean canUpdateBudget() {
        return isFormValid.get() && currentEditingBudget != null;
    }
    
    private boolean canDeleteBudget() {
        return selectedBudget.get() != null;
    }
    
    // Property getters
    public StringProperty budgetNameProperty() { return budgetName; }
    public StringProperty budgetDescriptionProperty() { return budgetDescription; }
    public StringProperty allocatedAmountProperty() { return allocatedAmount; }
    public ObjectProperty<String> selectedCategoryIdProperty() { return selectedCategoryId; }
    public ObjectProperty<BudgetType> selectedBudgetTypeProperty() { return selectedBudgetType; }
    public ObjectProperty<Period> selectedPeriodProperty() { return selectedPeriod; }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }
    public StringProperty warningThresholdProperty() { return warningThreshold; }
    
    // Collection getters
    public ObservableList<Budget> getBudgets() { return budgets; }
    public ObjectProperty<Budget> selectedBudgetProperty() { return selectedBudget; }
    public ObservableList<Budget> getOverBudgets() { return overBudgets; }
    public ObservableList<Budget> getNearThresholdBudgets() { return nearThresholdBudgets; }
    
    // Summary property getters
    public StringProperty totalAllocatedProperty() { return totalAllocated; }
    public StringProperty totalSpentProperty() { return totalSpent; }
    public StringProperty totalRemainingProperty() { return totalRemaining; }
    public StringProperty overallProgressProperty() { return overallProgress; }
    
    // Filter property getters
    public ObjectProperty<BudgetType> filterBudgetTypeProperty() { return filterBudgetType; }
    public BooleanProperty showOnlyActiveProperty() { return showOnlyActive; }
    public BooleanProperty showOnlyOverBudgetProperty() { return showOnlyOverBudget; }
    public StringProperty searchTextProperty() { return searchText; }
    
    // Validation property getters
    public BooleanProperty isFormValidProperty() { return isFormValid; }
    public StringProperty validationErrorsProperty() { return validationErrors; }
    
    // Command getters
    public Command getCreateBudgetCommand() { return createBudgetCommand; }
    public Command getUpdateBudgetCommand() { return updateBudgetCommand; }
    public Command getDeleteBudgetCommand() { return deleteBudgetCommand; }
    public Command getResetBudgetCommand() { return resetBudgetCommand; }
    public Command getRefreshDataCommand() { return refreshDataCommand; }
    public Command getExportBudgetsCommand() { return exportBudgetsCommand; }
    public Command getImportBudgetsCommand() { return importBudgetsCommand; }
    
    // Callback setters
    public void setOnBudgetCreated(Consumer<Budget> onBudgetCreated) {
        this.onBudgetCreated = onBudgetCreated;
    }
    
    public void setOnBudgetUpdated(Consumer<Budget> onBudgetUpdated) {
        this.onBudgetUpdated = onBudgetUpdated;
    }
    
    public void setOnBudgetDeleted(Consumer<String> onBudgetDeleted) {
        this.onBudgetDeleted = onBudgetDeleted;
    }
    
    public void setOnShowBudgetTemplates(Runnable onShowBudgetTemplates) {
        this.onShowBudgetTemplates = onShowBudgetTemplates;
    }
}