package com.mybudgetbuddy.controller;


import com.mybudgetbuddy.domain.model.Goal;
import com.mybudgetbuddy.domain.model.GoalStatus;
import com.mybudgetbuddy.domain.model.GoalType;
import com.mybudgetbuddy.viewmodel.GoalsViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Goals GUI - Using Dialog for Add/Edit
 */
public class GoalsController implements Initializable {
    
    // Action buttons
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    
    // Table
    @FXML private TableView<Goal> goalsTable;
    @FXML private TableColumn<Goal, String> nameColumn;
    @FXML private TableColumn<Goal, GoalType> typeColumn;
    @FXML private TableColumn<Goal, BigDecimal> targetAmountColumn;
    @FXML private TableColumn<Goal, BigDecimal> currentAmountColumn;
    @FXML private TableColumn<Goal, String> progressColumn;
    @FXML private TableColumn<Goal, LocalDate> targetDateColumn;
    @FXML private TableColumn<Goal, GoalStatus> statusColumn;
    
    // Status
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    private GoalsViewModel viewModel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupEventHandlers();
        setupResponsiveDesign();
    }
    
    public void setViewModel(GoalsViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            bindToViewModel();
        }
    }
    
    public GoalsViewModel getViewModel() {
        return viewModel;
    }
    
    private void setupTable() {
        // Configure table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        targetAmountColumn.setCellValueFactory(new PropertyValueFactory<>("targetAmount"));
        currentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        targetDateColumn.setCellValueFactory(new PropertyValueFactory<>("targetDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format progress column
        progressColumn.setCellValueFactory(cellData -> {
            Goal goal = cellData.getValue();
            if (goal.getTargetAmount() != null && goal.getCurrentAmount() != null) {
                double progress = goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
                return new javafx.beans.property.SimpleStringProperty(String.format("%.1f%%", progress));
            }
            return new javafx.beans.property.SimpleStringProperty("0.0%");
        });
        
        // Format currency columns
        targetAmountColumn.setCellFactory(column -> new TableCell<Goal, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        currentAmountColumn.setCellFactory(column -> new TableCell<Goal, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        // Make table responsive with new policy
        goalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }
    
    private void setupEventHandlers() {
        // Table selection handler
        goalsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (viewModel != null) {
                viewModel.selectedGoalProperty().set(newSelection);
            }
            updateButtonStates();
        });
        
        // Double-click to edit
        goalsTable.setRowFactory(tv -> {
            TableRow<Goal> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditGoal();
                }
            });
            return row;
        });
    }
    
    private void setupResponsiveDesign() {
        // Configure table to fill available space
        if (goalsTable != null) {
            goalsTable.setMaxWidth(Double.MAX_VALUE);
            goalsTable.setMaxHeight(Double.MAX_VALUE);
        }
    }
    
    private void bindToViewModel() {
        if (viewModel == null) return;
        
        // Bind table data
        goalsTable.setItems(viewModel.getGoalsList());
        
        // Bind status and loading
        if (statusLabel != null) {
            statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        }
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().bind(viewModel.isLoadingProperty());
        }
        
        updateButtonStates();
    }
    
    @FXML
    public void handleCreateGoal() {
        openGoalDialog(null);
    }
    
    @FXML
    public void handleEditGoal() {
        Goal selectedGoal = goalsTable.getSelectionModel().getSelectedItem();
        if (selectedGoal != null) {
            openGoalDialog(selectedGoal);
        } else {
            showAlert("No Selection", "Please select a goal to edit.");
        }
    }
    
    @FXML
    public void handleDeleteGoal() {
        Goal selectedGoal = goalsTable.getSelectionModel().getSelectedItem();
        if (selectedGoal == null) {
            showAlert("No Selection", "Please select a goal to delete.");
            return;
        }
        
        if (confirmDelete(selectedGoal) && viewModel != null) {
            viewModel.getDeleteGoalCommand().execute();
        }
    }
    
    @FXML
    private void handleRefresh() {
        if (viewModel != null) {
            viewModel.getRefreshCommand().execute();
        }
    }
    
    private void openGoalDialog(Goal goal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mybudgetbuddy/add-edit-goal.fxml"));
            DialogPane dialogPane = loader.load();
            GoalDialogController dialogController = loader.getController();
            dialogController.setViewModel(viewModel);
            dialogController.setGoal(goal);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(goal == null ? "Add Goal" : "Edit Goal");
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Goal goalToSave = dialogController.getGoal();
                if (goalToSave != null && viewModel != null) {
                    viewModel.persistGoal(goalToSave, goal == null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open goal dialog: " + e.getMessage());
        }
    }
    
    private boolean confirmDelete(Goal goal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Goal");
        confirm.setContentText("Are you sure you want to delete the goal '" + goal.getName() + "'?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void updateButtonStates() {
        boolean hasSelection = goalsTable.getSelectionModel().getSelectedItem() != null;
        
        if (createButton != null) createButton.setDisable(false);
        if (editButton != null) editButton.setDisable(!hasSelection);
        if (deleteButton != null) deleteButton.setDisable(!hasSelection);
        if (refreshButton != null) refreshButton.setDisable(false);
    }
}