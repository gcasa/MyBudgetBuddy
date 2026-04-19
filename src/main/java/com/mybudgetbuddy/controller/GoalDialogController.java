package com.mybudgetbuddy.controller;

import com.mybudgetbuddy.domain.model.Goal;
import com.mybudgetbuddy.domain.model.GoalStatus;
import com.mybudgetbuddy.domain.model.GoalType;
import com.mybudgetbuddy.domain.model.Priority;
import com.mybudgetbuddy.viewmodel.GoalsViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for the Goal Add/Edit Dialog
 */
public class GoalDialogController implements Initializable {
    
    // Form controls
    @FXML private TextField goalNameField;
    @FXML private ComboBox<GoalType> goalTypeCombo;
    @FXML private TextField targetAmountField;
    @FXML private TextField currentAmountField;
    @FXML private DatePicker targetDatePicker;
    @FXML private ComboBox<Priority> priorityCombo;
    @FXML private TextArea goalDescriptionArea;
    
    // Status controls
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    private GoalsViewModel viewModel;
    private Goal currentGoal;
    private boolean isEditMode = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupValidation();
    }
    
    public void setViewModel(GoalsViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            bindToViewModel();
        }
    }
    
    public void setGoal(Goal goal) {
        this.currentGoal = goal;
        this.isEditMode = (goal != null);
        
        if (isEditMode) {
            populateForm(goal);
        } else {
            clearForm();
        }
    }
    
    public Goal getGoal() {
        if (validateForm()) {
            return createGoalFromForm();
        }
        return null;
    }
    
    private void setupComboBoxes() {
        // Setup Goal Type ComboBox
        goalTypeCombo.getItems().addAll(GoalType.values());
        goalTypeCombo.setValue(GoalType.SAVINGS);
        
        // Setup Priority ComboBox
        priorityCombo.getItems().addAll(Priority.values());
        priorityCombo.setValue(Priority.MEDIUM);
    }
    
    private void setupValidation() {
        // Add validation listeners
        goalNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        targetAmountField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        
        // Format amount fields on focus lost
        targetAmountField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) formatAmountField(targetAmountField);
        });
        
        currentAmountField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) formatAmountField(currentAmountField);
        });
    }
    
    private void bindToViewModel() {
        if (viewModel == null) return;
        
        // Don't bind form fields to ViewModel properties for the dialog
        // The dialog maintains its own state separate from the main ViewModel
        
        // Only bind status and loading (these won't conflict)
        if (viewModel.statusMessageProperty() != null) {
            statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        }
        if (viewModel.isLoadingProperty() != null) {
            loadingIndicator.visibleProperty().bind(viewModel.isLoadingProperty());
        }
    }
    
    private void populateForm(Goal goal) {
        goalNameField.setText(goal.getName());
        goalDescriptionArea.setText(goal.getDescription() != null ? goal.getDescription() : "");
        goalTypeCombo.setValue(goal.getType());
        targetAmountField.setText(goal.getTargetAmount().toString());
        currentAmountField.setText(goal.getCurrentAmount().toString());
        targetDatePicker.setValue(goal.getTargetDate());
        priorityCombo.setValue(goal.getPriority());
    }
    
    private void clearForm() {
        goalNameField.setText("New Goal");
        goalDescriptionArea.setText("");
        goalTypeCombo.setValue(GoalType.SAVINGS);
        targetAmountField.setText("100.00");
        currentAmountField.setText("0.00");
        targetDatePicker.setValue(LocalDate.now().plusMonths(12));
        priorityCombo.setValue(Priority.MEDIUM);
    }
    
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Validate name
        if (goalNameField.getText().trim().isEmpty()) {
            errors.append("Goal name is required. ");
        }
        
        // Validate target amount
        try {
            BigDecimal target = new BigDecimal(targetAmountField.getText());
            if (target.compareTo(BigDecimal.ZERO) <= 0) {
                errors.append("Target amount must be greater than zero. ");
            }
        } catch (NumberFormatException e) {
            errors.append("Invalid target amount. ");
        }
        
        // Validate current amount
        try {
            BigDecimal current = new BigDecimal(currentAmountField.getText());
            if (current.compareTo(BigDecimal.ZERO) < 0) {
                errors.append("Current amount cannot be negative. ");
            }
        } catch (NumberFormatException e) {
            errors.append("Invalid current amount. ");
        }
        
        if (errors.length() > 0) {
            statusLabel.setText(errors.toString());
            statusLabel.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            statusLabel.setText("Ready to save");
            statusLabel.setStyle("-fx-text-fill: green;");
            return true;
        }
    }
    
    private Goal createGoalFromForm() {
        Goal goal = isEditMode ? currentGoal : new Goal();
        
        goal.setName(goalNameField.getText().trim());
        goal.setDescription(goalDescriptionArea.getText().trim());
        goal.setType(goalTypeCombo.getValue());
        goal.setTargetAmount(new BigDecimal(targetAmountField.getText()));
        goal.setCurrentAmount(new BigDecimal(currentAmountField.getText()));
        goal.setTargetDate(targetDatePicker.getValue());
        goal.setPriority(priorityCombo.getValue());
        
        if (!isEditMode) {
            goal.setStatus(GoalStatus.ACTIVE);
            goal.setCreatedDate(LocalDate.now());
            goal.setLastUpdated(LocalDate.now());
        } else {
            goal.setLastUpdated(LocalDate.now());
        }
        
        return goal;
    }
    
    private void formatAmountField(TextField field) {
        try {
            BigDecimal amount = new BigDecimal(field.getText());
            field.setText(String.format("%.2f", amount));
        } catch (NumberFormatException e) {
            // Leave as-is if invalid
        }
    }
}