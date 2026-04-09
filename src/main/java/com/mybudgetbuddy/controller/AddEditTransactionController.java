package com.mybudgetbuddy.controller;

import com.mybudgetbuddy.model.Transaction;
import com.mybudgetbuddy.model.TransactionType;
import com.mybudgetbuddy.viewmodel.AddEditTransactionViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddEditTransactionController {

    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<TransactionType> typeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private Button saveButton;

    private AddEditTransactionViewModel viewModel;

    public void setViewModel(AddEditTransactionViewModel viewModel) {
        this.viewModel = viewModel;
        bindToViewModel();
    }

    public void setTransaction(Transaction transaction) {
        if (viewModel != null) {
            viewModel.setTransaction(transaction);
        }
    }

    @FXML
    public void initialize() {
        // Initial setup will be completed in bindToViewModel()
    }
    
    private void bindToViewModel() {
        if (viewModel == null) return;
        
        // Bind form controls to ViewModel properties
        descriptionField.textProperty().bindBidirectional(viewModel.descriptionProperty());
        amountField.textProperty().bindBidirectional(viewModel.amountProperty());
        categoryComboBox.valueProperty().bindBidirectional(viewModel.selectedCategoryProperty());
        typeComboBox.valueProperty().bindBidirectional(viewModel.selectedTypeProperty());
        datePicker.valueProperty().bindBidirectional(viewModel.selectedDateProperty());
        
        // Set up ComboBox items
        categoryComboBox.setItems(viewModel.getCategories());
        typeComboBox.setItems(viewModel.getTransactionTypes());
        
        // Bind save button state
        if (saveButton != null) {
            saveButton.disableProperty().bind(viewModel.isValidProperty().not());
        }
    }

    @FXML
    private void handleSave() {
        if (viewModel != null) {
            viewModel.getSaveCommand().execute();
        }
    }
}
