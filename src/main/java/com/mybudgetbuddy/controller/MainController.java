package com.mybudgetbuddy.controller;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.application.service.TransactionService;
import com.mybudgetbuddy.application.service.ReportService;
import com.mybudgetbuddy.application.service.GoalService;
import com.mybudgetbuddy.application.service.impl.ReportServiceImpl;
import com.mybudgetbuddy.application.service.impl.GoalServiceImpl;
import com.mybudgetbuddy.viewmodel.MainViewModel;
import com.mybudgetbuddy.viewmodel.TransactionViewModel;
import com.mybudgetbuddy.viewmodel.ReportsViewModel;
import com.mybudgetbuddy.viewmodel.GoalsViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, TransactionType> typeColumn;
    @FXML private TableColumn<Transaction, BigDecimal> amountColumn;

    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label balanceLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button reportsButton;
    
    // Tab management
    @FXML private TabPane mainTabPane;
    @FXML private Tab transactionsTab;
    @FXML private Tab reportsTab;
    @FXML private Tab goalsTab;

    private MainViewModel viewModel;
    private TransactionService transactionService;
    private ReportService reportService;
    private GoalService goalService;
    private ReportsViewModel reportsViewModel;
    private GoalsViewModel goalsViewModel;
    private ReportsController reportsController;
    private GoalsController goalsController;
    private boolean reportsTabInitialized = false;
    private boolean goalsTabInitialized = false;

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        bindToViewModel();
    }
    
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
        
        // Initialize report service and viewmodel
        this.reportService = new ReportServiceImpl();
        this.reportsViewModel = new ReportsViewModel(reportService);
        
        // Initialize goal service and viewmodel
        this.goalService = new GoalServiceImpl(); 
        this.goalsViewModel = new GoalsViewModel(goalService);
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTabNavigation();
    }
    
    private void setupTabNavigation() {
        // Initialize tabs
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldTab, newTab) -> {
                if (newTab == reportsTab && !reportsTabInitialized) {
                    initializeReportsTab();
                } else if (newTab == goalsTab && !goalsTabInitialized) {
                    initializeGoalsTab();
                }
                // Update toolbar button labels based on active tab
                updateToolbarLabels(newTab);
            }
        );
    }
    
    private void initializeReportsTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mybudgetbuddy/reports.fxml"));
            BorderPane reportsContent = loader.load();
            
            reportsController = loader.getController();
            reportsController.setViewModel(reportsViewModel);
            
            reportsTab.setContent(reportsContent);
            reportsTabInitialized = true;
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load Reports", 
                     "Could not initialize the Reports tab: " + e.getMessage());
        }
    }
    
    private void initializeGoalsTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mybudgetbuddy/goals.fxml"));
            BorderPane goalsContent = loader.load();
            
            goalsController = loader.getController();
            goalsController.setViewModel(goalsViewModel);
            goalsController.setGoalService(goalService);
            
            goalsTab.setContent(goalsContent);
            goalsTabInitialized = true;
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load Goals", 
                     "Could not initialize the Goals tab: " + e.getMessage());
        }
    }
    
    private void updateToolbarLabels(Tab activeTab) {
        if (activeTab == goalsTab) {
            // Goals context
            addButton.setText("＋ Add Goal");
            editButton.setText("✎ Edit Goal");
            deleteButton.setText("✕ Delete Goal");
        } else {
            // Transaction context (default)
            addButton.setText("＋ Add");
            editButton.setText("✎ Edit");
            deleteButton.setText("✕ Delete");
        }
    }
    
    private void bindToViewModel() {
        if (viewModel == null) return;
        
        // Bind data
        transactionTable.setItems(viewModel.getTransactions());
        totalIncomeLabel.textProperty().bind(viewModel.totalIncomeProperty());
        totalExpensesLabel.textProperty().bind(viewModel.totalExpensesProperty());
        balanceLabel.textProperty().bind(viewModel.balanceProperty());
        
        // Bind selected transaction
        viewModel.selectedTransactionProperty().bind(
            transactionTable.getSelectionModel().selectedItemProperty()
        );
        
        // Bind button states
        editButton.disableProperty().bind(
            viewModel.selectedTransactionProperty().isNull()
        );
        deleteButton.disableProperty().bind(
            viewModel.selectedTransactionProperty().isNull()
        );
        
        // Set up ViewModel callbacks
        viewModel.setOnOpenAddEditDialog(this::openDialog);
        viewModel.setOnConfirmDelete(this::confirmDelete);
        
        // Update balance label styling
        viewModel.balanceProperty().addListener((obs, oldVal, newVal) -> updateBalanceStyle(newVal));
        
        // Initial style update
        updateBalanceStyle(viewModel.balanceProperty().get());
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
            }
        });

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(col -> new TableCell<Transaction, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    setText(String.format("$%.2f", item.doubleValue()));
                    if (t.getType() == TransactionType.INCOME) {
                        setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }
    
    private void updateBalanceStyle(String balanceText) {
        if (balanceText == null) return;
        
        // Extract the numeric value to determine color
        try {
            String numericPart = balanceText.replace("Balance: $", "").replace(",", "");
            double balance = Double.parseDouble(numericPart);
            
            if (balance >= 0) {
                balanceLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            } else {
                balanceLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            }
        } catch (NumberFormatException e) {
            // Default style if parsing fails
            balanceLabel.setStyle("");
        }
    }

    @FXML
    private void handleAdd() {
        // Context-aware Add button based on active tab
        if (mainTabPane.getSelectionModel().getSelectedItem() == goalsTab) {
            // Add Goal via dialog
            if (goalsController != null) {
                goalsController.handleCreateGoal();
            }
        } else {
            // Add Transaction (default)
            if (viewModel != null) {
                viewModel.getAddCommand().execute();
            }
        }
    }

    @FXML
    private void handleEdit() {
        // Context-aware Edit button based on active tab
        if (mainTabPane.getSelectionModel().getSelectedItem() == goalsTab) {
            // Edit Goal via dialog
            if (goalsController != null) {
                goalsController.handleEditGoal();
            }
        } else {
            // Edit Transaction (default)
            if (viewModel != null) {
                viewModel.getEditCommand().execute();
            }
        }
    }

    @FXML
    private void handleDelete() {
        // Context-aware Delete button based on active tab
        if (mainTabPane.getSelectionModel().getSelectedItem() == goalsTab) {
            // Delete Goal
            if (goalsController != null) {
                goalsController.handleDeleteGoal();
            }
        } else {
            // Delete Transaction (default)
            if (viewModel != null) {
                viewModel.getDeleteCommand().execute();
            }
        }
    }
    
    @FXML
    private void handleReports() {
        // Switch to reports tab
        mainTabPane.getSelectionModel().select(reportsTab);
        
        // Initialize reports tab if not already done
        if (!reportsTabInitialized) {
            initializeReportsTab();
        }
    }
    
    private boolean confirmDelete() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) return false;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Transaction");
        confirm.setContentText("Are you sure you want to delete \"" + selected.getDescription() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void openDialog(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mybudgetbuddy/add-edit-transaction.fxml"));
            DialogPane dialogPane = loader.load();
            TransactionController controller = loader.getController();
            
            // Create ViewModel for the dialog
            TransactionViewModel dialogViewModel = new TransactionViewModel(transactionService);
            
            controller.setViewModel(dialogViewModel);
            controller.setTransaction(transaction);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(transaction == null ? "Add Transaction" : "Edit Transaction");
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Execute save command and refresh if successful
                dialogViewModel.getSaveCommand().execute();
                if (viewModel != null) {
                    viewModel.refreshData();
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Could not open the transaction dialog: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
