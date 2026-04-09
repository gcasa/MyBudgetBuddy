package com.mybudgetbuddy.viewmodel;

import com.mybudgetbuddy.command.Command;
import com.mybudgetbuddy.command.RelayCommand;
import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.application.service.TransactionService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import static java.util.List.of;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class AddEditTransactionViewModel {
    
    private static final String OTHER = "Other";
    private final TransactionService transactionService;
    private Transaction transaction;

    private static final List<String> DEFAULT_CATEGORIES = of(
            "Food", "Housing", "Transportation", "Entertainment",
            "Healthcare", "Education", "Income", OTHER
    );
    
    // Form properties
    private final StringProperty description;
    private final StringProperty amount;
    private final ObjectProperty<String> selectedCategory;
    private final ObjectProperty<TransactionType> selectedType;
    private final ObjectProperty<LocalDate> selectedDate;
    
    // Available options
    private final ObservableList<String> categories;
    private final ObservableList<TransactionType> transactionTypes;
    
    // Validation
    private final BooleanProperty isValid;
    private final StringProperty validationErrors;
    
    // Commands
    private final Command saveCommand;
    
    // Callback for successful save
    private Consumer<Boolean> onSaveCompleted;
    
    public AddEditTransactionViewModel(TransactionService transactionService) {
        this.transactionService = transactionService;
        
        // Initialize form properties
        this.description = new SimpleStringProperty("");
        this.amount = new SimpleStringProperty("");
        this.selectedCategory = new SimpleObjectProperty<>(OTHER);
        this.selectedType = new SimpleObjectProperty<>(TransactionType.EXPENSE);
        this.selectedDate = new SimpleObjectProperty<>(LocalDate.now());
        
        // Initialize available options
        this.categories = FXCollections.observableArrayList(DEFAULT_CATEGORIES);
        this.transactionTypes = FXCollections.observableArrayList(TransactionType.values());
        
        // Initialize validation properties
        this.validationErrors = new SimpleStringProperty("");
        this.isValid = new SimpleBooleanProperty(false);
        
        // Set up validation binding
        setupValidation();
        
        // Initialize commands
        this.saveCommand = new RelayCommand(this::handleSave, isValid::get);
    }
    
    // Property getters for binding
    public StringProperty descriptionProperty() { return description; }
    public StringProperty amountProperty() { return amount; }
    public ObjectProperty<String> selectedCategoryProperty() { return selectedCategory; }
    public ObjectProperty<TransactionType> selectedTypeProperty() { return selectedType; }
    public ObjectProperty<LocalDate> selectedDateProperty() { return selectedDate; }
    
    public ObservableList<String> getCategories() { return categories; }
    public ObservableList<TransactionType> getTransactionTypes() { return transactionTypes; }
    
    public BooleanProperty isValidProperty() { return isValid; }
    public StringProperty validationErrorsProperty() { return validationErrors; }
    
    // Command getters
    public Command getSaveCommand() { return saveCommand; }
    
    // Callback setter
    public void setOnSaveCompleted(Consumer<Boolean> onSaveCompleted) {
        this.onSaveCompleted = onSaveCompleted;
    }
    
    // Public methods
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        if (transaction != null) {
            description.set(transaction.getDescription());
            amount.set(String.valueOf(transaction.getAmount()));
            selectedCategory.set(transaction.getCategory());
            selectedType.set(transaction.getType());
            selectedDate.set(transaction.getDate());
        } else {
            // Reset to defaults for new transaction
            description.set("");
            amount.set("");
            selectedCategory.set(OTHER);
            selectedType.set(TransactionType.EXPENSE);
            selectedDate.set(LocalDate.now());
        }
    }
    
    private void setupValidation() {
        isValid.bind(Bindings.createBooleanBinding(() -> {
            // Update validation errors and return validity
            StringBuilder errors = new StringBuilder();
            
            if (description.get() == null || description.get().trim().isEmpty()) {
                errors.append("- Description is required.\n");
            }
            
            String amountText = amount.get();
            if (amountText == null || amountText.trim().isEmpty()) {
                errors.append("- Amount is required.\n");
            } else {
                try {
                    double val = Double.parseDouble(amountText.trim());
                    if (val <= 0) {
                        errors.append("- Amount must be greater than zero.\n");
                    }
                } catch (NumberFormatException e) {
                    errors.append("- Amount must be a valid number.\n");
                }
            }
            
            if (selectedCategory.get() == null) {
                errors.append("- Category is required.\n");
            }
            
            if (selectedType.get() == null) {
                errors.append("- Type is required.\n");
            }
            
            if (selectedDate.get() == null) {
                errors.append("- Date is required.\n");
            } else if (selectedDate.get().isAfter(LocalDate.now())) {
                errors.append("- Date cannot be in the future.\n");
            }
            
            validationErrors.set(errors.toString());
            return errors.isEmpty();
            
        }, description, amount, selectedCategory, selectedType, selectedDate));
    }
    
    private void handleSave() {
        if (!isValid.get()) {
            showValidationError();
            return;
        }
        
        try {
            String desc = description.get().trim();
            double amt = Double.parseDouble(amount.get().trim());
            BigDecimal amountDecimal = BigDecimal.valueOf(amt);
            String category = selectedCategory.get();
            TransactionType type = selectedType.get();
            LocalDate date = selectedDate.get();
            
            if (transaction == null) {
                // Create new transaction
                Transaction newTransaction = new Transaction(desc, amountDecimal, type, category, date);
                transactionService.createTransaction(newTransaction);
            } else {
                // Update existing transaction
                transaction.setDescription(desc);
                transaction.setAmount(amountDecimal);
                transaction.setCategory(category);
                transaction.setType(type);
                transaction.setDate(date);
                transactionService.updateTransaction(transaction);
            }
            
            if (onSaveCompleted != null) {
                onSaveCompleted.accept(true);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Error");
            alert.setHeaderText("Failed to save transaction");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            
            if (onSaveCompleted != null) {
                onSaveCompleted.accept(false);
            }
        }
    }
    
    private void showValidationError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please fix the following errors:");
        alert.setContentText(validationErrors.get());
        alert.showAndWait();
    }
}