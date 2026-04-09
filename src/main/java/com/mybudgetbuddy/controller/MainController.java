package com.mybudgetbuddy.controller;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.service.BudgetService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, TransactionType> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;

    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label balanceLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private BudgetService budgetService;

    public void setBudgetService(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @FXML
    public void initialize() {
        if (budgetService == null) return;
        setupTableColumns();
        refreshTable();
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
        amountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction t = getTableView().getItems().get(getIndex());
                    setText(String.format("$%.2f", item));
                    if (t.getType() == TransactionType.INCOME) {
                        setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void refreshTable() {
        transactionTable.setItems(
                FXCollections.observableArrayList(budgetService.getTransactions()));
        updateSummary();
    }

    private void updateSummary() {
        double income = budgetService.getTotalIncome();
        double expenses = budgetService.getTotalExpenses();
        double balance = budgetService.getBalance();

        totalIncomeLabel.setText(String.format("Income: $%.2f", income));
        totalExpensesLabel.setText(String.format("Expenses: $%.2f", expenses));
        balanceLabel.setText(String.format("Balance: $%.2f", balance));

        if (balance >= 0) {
            balanceLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        } else {
            balanceLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleAdd() {
        openDialog(null);
    }

    @FXML
    private void handleEdit() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a transaction to edit.");
            return;
        }
        openDialog(selected);
    }

    @FXML
    private void handleDelete() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a transaction to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Transaction");
        confirm.setContentText("Are you sure you want to delete \"" + selected.getDescription() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            budgetService.deleteTransaction(selected.getId());
            refreshTable();
        }
    }

    private void openDialog(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mybudgetbuddy/add-edit-transaction.fxml"));
            DialogPane dialogPane = loader.load();
            AddEditTransactionController controller = loader.getController();
            controller.setBudgetService(budgetService);
            controller.setTransaction(transaction);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(transaction == null ? "Add Transaction" : "Edit Transaction");
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                controller.handleSave();
                refreshTable();
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
}
