package com.mybudgetbuddy.controller;

import com.mybudgetbuddy.application.service.GoalService;
import com.mybudgetbuddy.domain.model.Goal;
import com.mybudgetbuddy.domain.model.GoalStatus;
import com.mybudgetbuddy.domain.model.GoalType;
import com.mybudgetbuddy.domain.model.Priority;
import com.mybudgetbuddy.viewmodel.GoalsViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Goals GUI
 */
public class GoalsController implements Initializable {
    
    // Form controls
    @FXML private TextField goalNameField;
    @FXML private TextArea goalDescriptionArea;
    @FXML private ComboBox<GoalType> goalTypeCombo;
    @FXML private TextField targetAmountField;
    @FXML private TextField currentAmountField;
    @FXML private DatePicker targetDatePicker;
    @FXML private ComboBox<Priority> priorityCombo;
    @FXML private TextField monthlyContributionField;
    
    // Action buttons
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button addContributionButton;
    @FXML private Button markCompleteButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button duplicateButton;
    
    // Table and selection
    @FXML private TableView<Goal> goalsTable;
    @FXML private TableColumn<Goal, String> nameColumn;
    @FXML private TableColumn<Goal, GoalType> typeColumn;
    @FXML private TableColumn<Goal, BigDecimal> targetAmountColumn;
    @FXML private TableColumn<Goal, BigDecimal> currentAmountColumn;
    @FXML private TableColumn<Goal, String> progressColumn;
    @FXML private TableColumn<Goal, LocalDate> targetDateColumn;
    @FXML private TableColumn<Goal, Priority> priorityColumn;
    @FXML private TableColumn<Goal, GoalStatus> statusColumn;
    
    // Filter controls
    @FXML private ComboBox<GoalType> filterTypeCombo;
    @FXML private ComboBox<GoalStatus> filterStatusCombo;
    @FXML private ComboBox<Priority> filterPriorityCombo;
    @FXML private CheckBox showCompletedCheckBox;
    
    // Details and statistics
    @FXML private Label selectedGoalNameLabel;
    @FXML private Label selectedGoalDescriptionLabel;
    @FXML private Label selectedGoalProgressLabel;
    @FXML private Label selectedGoalRemainingLabel;
    @FXML private Label selectedGoalDaysRemainingLabel;
    @FXML private Label selectedGoalRequiredMonthlyLabel;
    @FXML private ProgressBar selectedGoalProgressBar;
    
    @FXML private Label totalGoalsLabel;
    @FXML private Label activeGoalsLabel;
    @FXML private Label completedGoalsLabel;
    @FXML private Label totalTargetAmountLabel;
    @FXML private Label totalCurrentAmountLabel;
    @FXML private Label overallProgressLabel;
    @FXML private ProgressBar overallProgressBar;
    
    @FXML private VBox formContainer;
    @FXML private VBox detailsContainer;
    
    // Status and progress
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    private GoalsViewModel viewModel;
    private GoalService goalService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBoxes();
        setupEventHandlers();
    }
    
    public void setViewModel(GoalsViewModel viewModel) {
        this.viewModel = viewModel;
        bindToViewModel();
        updateButtonStates();
    }
    
    public void setGoalService(GoalService goalService) {
        this.goalService = goalService;
    }
    
    private void setupTable() {
        // Configure table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        targetAmountColumn.setCellValueFactory(new PropertyValueFactory<>("targetAmount"));
        currentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        targetDateColumn.setCellValueFactory(new PropertyValueFactory<>("targetDate"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Custom cell factory for progress column
        progressColumn.setCellValueFactory(cellData -> {
            Goal goal = cellData.getValue();
            BigDecimal progress = goal.getProgressPercentage();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f%%", progress.doubleValue())
            );
        });
        
        // Custom cell factory for amount formatting
        targetAmountColumn.setCellFactory(column -> new TableCell<Goal, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", amount.doubleValue()));
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
                    setText(String.format("$%,.2f", amount.doubleValue()));
                }
            }
        });
        
        // Custom cell factory for status with color coding
        statusColumn.setCellFactory(column -> new TableCell<Goal, GoalStatus>() {
            @Override
            protected void updateItem(GoalStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.name());
                    switch (status) {
                        case COMPLETED:
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case OVERDUE:
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case PAUSED:
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case ACTIVE:
                            setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        });
        
        // Setup table selection listener
        goalsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (viewModel != null) {
                    viewModel.selectedGoalProperty().set(newSelection);
                    updateGoalDetails(newSelection);
                    updateButtonStates();
                }
            });
    }
    
    private void setupComboBoxes() {
        // Goal Type ComboBox
        goalTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(GoalType type) {
                return type != null ? type.name().replace("_", " ") : "";
            }
            
            @Override
            public GoalType fromString(String string) {
                return GoalType.valueOf(string.replace(" ", "_"));
            }
        });
        
        // Priority ComboBox
        priorityCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Priority priority) {
                return priority != null ? priority.name() : "";
            }
            
            @Override
            public Priority fromString(String string) {
                return Priority.valueOf(string);
            }
        });
        
        // Filter ComboBoxes
        filterTypeCombo.setConverter(goalTypeCombo.getConverter());
        filterStatusCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(GoalStatus status) {
                return status != null ? status.name().replace("_", " ") : "";
            }
            
            @Override
            public GoalStatus fromString(String string) {
                return GoalStatus.valueOf(string.replace(" ", "_"));
            }
        });
        filterPriorityCombo.setConverter(priorityCombo.getConverter());
    }
    
    private void setupEventHandlers() {
        // Add "All" option to filter combos
        filterTypeCombo.getItems().add(0, null);
        filterStatusCombo.getItems().add(0, null);
        filterPriorityCombo.getItems().add(0, null);
    }
    
    private void bindToViewModel() {
        if (viewModel == null) return;
        
        // Bind table data
        goalsTable.setItems(viewModel.getGoalsList());
        
        // Bind form fields
        goalNameField.textProperty().bindBidirectional(viewModel.goalNameProperty());
        goalDescriptionArea.textProperty().bindBidirectional(viewModel.goalDescriptionProperty());
        goalTypeCombo.valueProperty().bindBidirectional(viewModel.selectedGoalTypeProperty());
        targetAmountField.textProperty().bindBidirectional(viewModel.targetAmountTextProperty());
        currentAmountField.textProperty().bindBidirectional(viewModel.currentAmountTextProperty());
        targetDatePicker.valueProperty().bindBidirectional(viewModel.targetDateProperty());
        priorityCombo.valueProperty().bindBidirectional(viewModel.selectedPriorityProperty());
        monthlyContributionField.textProperty().bindBidirectional(viewModel.monthlyContributionTextProperty());
        
        // Bind combo box data
        goalTypeCombo.setItems(viewModel.getAvailableGoalTypes());
        priorityCombo.setItems(viewModel.getAvailablePriorities());
        filterTypeCombo.setItems(viewModel.getAvailableGoalTypes());
        filterStatusCombo.setItems(viewModel.getAvailableStatuses());
        filterPriorityCombo.setItems(viewModel.getAvailablePriorities());
        
        // Bind filter controls
        filterTypeCombo.valueProperty().bindBidirectional(viewModel.filterTypeProperty());
        filterStatusCombo.valueProperty().bindBidirectional(viewModel.filterStatusProperty());
        filterPriorityCombo.valueProperty().bindBidirectional(viewModel.filterPriorityProperty());
        showCompletedCheckBox.selectedProperty().bindBidirectional(viewModel.showCompletedGoalsProperty());
        
        // Bind statistics
        totalGoalsLabel.textProperty().bind(viewModel.totalGoalsProperty());
        activeGoalsLabel.textProperty().bind(viewModel.activeGoalsProperty());
        completedGoalsLabel.textProperty().bind(viewModel.completedGoalsProperty());
        totalTargetAmountLabel.textProperty().bind(viewModel.totalTargetAmountProperty());
        totalCurrentAmountLabel.textProperty().bind(viewModel.totalCurrentAmountProperty());
        overallProgressLabel.textProperty().bind(viewModel.overallProgressProperty());
        
        // Bind status and loading
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        loadingIndicator.visibleProperty().bind(viewModel.isLoadingProperty());
        
        // Bind form visibility
        formContainer.visibleProperty().bind(viewModel.isEditingProperty());
        formContainer.managedProperty().bind(viewModel.isEditingProperty());
        
        // Setup callbacks
        viewModel.setOnShowMessage(this::showMessage);
        viewModel.setOnShowError(this::showError);
    }
    
    // FXML Event Handlers
    @FXML
    private void handleCreateGoal() {
        if (viewModel != null) {
            viewModel.getCreateGoalCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleEditGoal() {
        if (viewModel != null) {
            viewModel.getEditGoalCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleSaveGoal() {
        if (viewModel != null) {
            viewModel.getSaveGoalCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleCancelEdit() {
        if (viewModel != null) {
            viewModel.getCancelEditCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleDeleteGoal() {
        if (viewModel != null) {
            Goal selected = viewModel.selectedGoalProperty().get();
            if (selected != null) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Delete Goal");
                confirmation.setHeaderText("Delete Goal: " + selected.getName());
                confirmation.setContentText("Are you sure you want to delete this goal? This action cannot be undone.");
                
                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    viewModel.getDeleteGoalCommand().execute();
                }
            }
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleRefresh() {
        if (viewModel != null) {
            viewModel.getRefreshCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleAddContribution() {
        if (viewModel != null) {
            Goal selected = viewModel.selectedGoalProperty().get();
            if (selected != null) {
                TextInputDialog dialog = new TextInputDialog("0.00");
                dialog.setTitle("Add Contribution");
                dialog.setHeaderText("Add Contribution to: " + selected.getName());
                dialog.setContentText("Enter contribution amount:");
                
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        viewModel.currentAmountTextProperty().set(result.get());
                        viewModel.getAddContributionCommand().execute();
                    } catch (NumberFormatException e) {
                        showError("Invalid amount entered");
                    }
                }
            }
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleMarkComplete() {
        if (viewModel != null) {
            Goal selected = viewModel.selectedGoalProperty().get();
            if (selected != null) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Mark Goal Complete");
                confirmation.setHeaderText("Mark as Complete: " + selected.getName());
                confirmation.setContentText("Are you sure you want to mark this goal as complete?");
                
                Optional<ButtonType> result = confirmation.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    viewModel.getMarkCompleteCommand().execute();
                }
            }
            updateButtonStates();
        }
    }
    
    @FXML
    private void handlePauseGoal() {
        if (viewModel != null) {
            viewModel.getPauseGoalCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleResumeGoal() {
        if (viewModel != null) {
            viewModel.getResumeGoalCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleDuplicateGoal() {
        if (viewModel != null) {
            viewModel.getDuplicateGoalCommand().execute();
            updateButtonStates();
        }
    }
    
    private void updateGoalDetails(Goal goal) {
        if (goal == null) {
            selectedGoalNameLabel.setText("");
            selectedGoalDescriptionLabel.setText("");
            selectedGoalProgressLabel.setText("");
            selectedGoalRemainingLabel.setText("");
            selectedGoalDaysRemainingLabel.setText("");
            selectedGoalRequiredMonthlyLabel.setText("");
            selectedGoalProgressBar.setProgress(0);
            return;
        }
        
        selectedGoalNameLabel.setText(goal.getName());
        selectedGoalDescriptionLabel.setText(goal.getDescription() != null ? goal.getDescription() : "No description");
        
        BigDecimal progress = goal.getProgressPercentage();
        selectedGoalProgressLabel.setText(String.format("%.1f%% Complete", progress.doubleValue()));
        selectedGoalProgressBar.setProgress(progress.doubleValue() / 100.0);
        
        BigDecimal remaining = goal.getRemainingAmount();
        selectedGoalRemainingLabel.setText(String.format("$%,.2f remaining", remaining.doubleValue()));
        
        long daysRemaining = goal.getDaysRemaining();
        selectedGoalDaysRemainingLabel.setText(String.format("%d days remaining", daysRemaining));
        
        BigDecimal requiredMonthly = goal.getRequiredMonthlyContribution();
        selectedGoalRequiredMonthlyLabel.setText(
            String.format("Required monthly: $%,.2f", requiredMonthly.doubleValue())
        );
    }
    
    private void updateButtonStates() {
        if (viewModel != null) {
            createButton.setDisable(!viewModel.getCreateGoalCommand().canExecute());
            editButton.setDisable(!viewModel.getEditGoalCommand().canExecute());
            saveButton.setDisable(!viewModel.getSaveGoalCommand().canExecute());
            cancelButton.setDisable(!viewModel.getCancelEditCommand().canExecute());
            deleteButton.setDisable(!viewModel.getDeleteGoalCommand().canExecute());
            refreshButton.setDisable(!viewModel.getRefreshCommand().canExecute());
            addContributionButton.setDisable(!viewModel.getAddContributionCommand().canExecute());
            markCompleteButton.setDisable(!viewModel.getMarkCompleteCommand().canExecute());
            pauseButton.setDisable(!viewModel.getPauseGoalCommand().canExecute());
            resumeButton.setDisable(!viewModel.getResumeGoalCommand().canExecute());
            duplicateButton.setDisable(!viewModel.getDuplicateGoalCommand().canExecute());
        }
    }
    
    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(error);
        alert.showAndWait();
    }
}