package com.mybudgetbuddy.viewmodel;

import com.mybudgetbuddy.command.Command;
import com.mybudgetbuddy.command.RelayCommand;
import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.application.service.TransactionService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MainViewModel {
    
    private final TransactionService transactionService;
    
    // Observable properties for data binding
    private final ObservableList<Transaction> transactions;
    private final StringProperty totalIncome;
    private final StringProperty totalExpenses; 
    private final StringProperty balance;
    private final ObjectProperty<Transaction> selectedTransaction;
    
    // Commands (actions)
    private final Command addCommand;
    private final Command editCommand;
    private final Command deleteCommand;
    
    // Callbacks for navigation
    private Consumer<Transaction> onOpenAddEditDialog;
    private Supplier<Boolean> onConfirmDelete;
    
    public MainViewModel(TransactionService transactionService) {
        this.transactionService = transactionService;
        
        // Initialize observable properties
        this.transactions = FXCollections.observableArrayList();
        this.totalIncome = new SimpleStringProperty();
        this.totalExpenses = new SimpleStringProperty();
        this.balance = new SimpleStringProperty();
        this.selectedTransaction = new SimpleObjectProperty<>();
        
        // Initialize commands
        this.addCommand = new RelayCommand(this::handleAdd);
        this.editCommand = new RelayCommand(this::handleEdit, this::canEdit);
        this.deleteCommand = new RelayCommand(this::handleDelete, this::canDelete);
        
        // Load initial data 
        refreshData();
    }
    
    // Property getters for binding
    public ObservableList<Transaction> getTransactions() { 
        return transactions; 
    }
    
    public StringProperty totalIncomeProperty() { 
        return totalIncome; 
    }
    
    public StringProperty totalExpensesProperty() { 
        return totalExpenses; 
    }
    
    public StringProperty balanceProperty() { 
        return balance; 
    }
    
    public ObjectProperty<Transaction> selectedTransactionProperty() { 
        return selectedTransaction; 
    }
    
    // Command getters
    public Command getAddCommand() { 
        return addCommand; 
    }
    
    public Command getEditCommand() { 
        return editCommand; 
    }
    
    public Command getDeleteCommand() { 
        return deleteCommand; 
    }
    
    // Callback setters
    public void setOnOpenAddEditDialog(Consumer<Transaction> onOpenAddEditDialog) {
        this.onOpenAddEditDialog = onOpenAddEditDialog;
    }
    
    public void setOnConfirmDelete(Supplier<Boolean> onConfirmDelete) {
        this.onConfirmDelete = onConfirmDelete;
    }
    
    // Command handlers
    private void handleAdd() {
        if (onOpenAddEditDialog != null) {
            onOpenAddEditDialog.accept(null);
        }
    }
    
    private void handleEdit() {
        if (onOpenAddEditDialog != null && selectedTransaction.get() != null) {
            onOpenAddEditDialog.accept(selectedTransaction.get());
        }
    }
    
    private void handleDelete() {
        Transaction selected = selectedTransaction.get();
        if (selected == null) {
            showAlert("No Selection", "Please select a transaction to delete.");
            return;
        }
        
        if (onConfirmDelete != null && onConfirmDelete.get()) {
            transactionService.deleteTransaction(selected.getId());
            refreshData();
        }
    }
    
    private boolean canEdit() {
        return selectedTransaction.get() != null;
    }
    
    private boolean canDelete() {
        return selectedTransaction.get() != null;
    }
    
    // Public methods for data operations
    public void refreshData() {
        transactions.setAll(transactionService.getAllTransactions());
        updateSummary();
    }
    
    private void updateSummary() {
        double income = transactionService.getTotalIncome().doubleValue();
        double expenses = transactionService.getTotalExpenses().doubleValue();
        double balanceValue = transactionService.getBalance().doubleValue();
        
        totalIncome.set(String.format("Income: $%.2f", income));
        totalExpenses.set(String.format("Expenses: $%.2f", expenses));
        balance.set(String.format("Balance: $%.2f", balanceValue));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}