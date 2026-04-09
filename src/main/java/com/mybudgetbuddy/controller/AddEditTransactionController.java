package com.mybudgetbuddy.controller;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.service.BudgetService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

public class AddEditTransactionController {

    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<TransactionType> typeComboBox;
    @FXML private DatePicker datePicker;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Food", "Housing", "Transportation", "Entertainment",
            "Healthcare", "Education", "Income", "Other"
    );

    private BudgetService budgetService;
    private Transaction transaction;

    public void setBudgetService(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        if (transaction != null) {
            descriptionField.setText(transaction.getDescription());
            amountField.setText(String.valueOf(transaction.getAmount()));
            categoryComboBox.setValue(transaction.getCategory());
            typeComboBox.setValue(transaction.getType());
            datePicker.setValue(transaction.getDate());
        }
    }

    @FXML
    public void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(DEFAULT_CATEGORIES));
        typeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        typeComboBox.setValue(TransactionType.EXPENSE);
        datePicker.setValue(LocalDate.now());
        categoryComboBox.setValue("Other");
    }

    public boolean handleSave() {
        if (!validate()) return false;

        String description = descriptionField.getText().trim();
        double amount = Double.parseDouble(amountField.getText().trim());
        String category = categoryComboBox.getValue();
        TransactionType type = typeComboBox.getValue();
        LocalDate date = datePicker.getValue();

        if (transaction == null) {
            Transaction newTransaction = new Transaction(description, amount, type, category, date);
            budgetService.addTransaction(newTransaction);
        } else {
            transaction.setDescription(description);
            transaction.setAmount(amount);
            transaction.setCategory(category);
            transaction.setType(type);
            transaction.setDate(date);
            budgetService.updateTransaction(transaction);
        }
        return true;
    }

    private boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            errors.append("- Description is required.\n");
        }

        String amountText = amountField.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            errors.append("- Amount is required.\n");
        } else {
            try {
                double val = Double.parseDouble(amountText.trim());
                if (val <= 0) errors.append("- Amount must be greater than zero.\n");
            } catch (NumberFormatException e) {
                errors.append("- Amount must be a valid number.\n");
            }
        }

        if (categoryComboBox.getValue() == null) {
            errors.append("- Category is required.\n");
        }

        if (typeComboBox.getValue() == null) {
            errors.append("- Type is required.\n");
        }

        if (datePicker.getValue() == null) {
            errors.append("- Date is required.\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fix the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }
}
