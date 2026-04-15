package com.mybudgetbuddy.controller;

import com.mybudgetbuddy.domain.model.Report;
import com.mybudgetbuddy.domain.model.ReportFormat;
import com.mybudgetbuddy.domain.model.ReportStatus;
import com.mybudgetbuddy.domain.model.ReportType;
import com.mybudgetbuddy.viewmodel.ReportsViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Reports view, handling UI interactions and binding to ReportsViewModel
 */
public class ReportsController {

    // Top toolbar controls
    @FXML private Button generateButton;
    @FXML private Button templateButton;
    @FXML private Button refreshButton;
    @FXML private Button deleteButton;
    @FXML private Button viewButton;
    @FXML private Button downloadButton;
    @FXML private Button shareButton;
    
    // Report generation form
    @FXML private TextField reportNameField;
    @FXML private ComboBox<ReportType> reportTypeCombo;
    @FXML private ComboBox<ReportFormat> formatCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> templateCombo;
    
    // Filters
    @FXML private TextField filterField;
    @FXML private ComboBox<ReportStatus> statusFilterCombo;
    @FXML private Button clearFiltersButton;
    
    // Reports table
    @FXML private TableView<Report> reportsTable;
    @FXML private TableColumn<Report, String> nameColumn;
    @FXML private TableColumn<Report, ReportType> typeColumn;
    @FXML private TableColumn<Report, ReportStatus> statusColumn;
    @FXML private TableColumn<Report, ReportFormat> formatColumn;
    @FXML private TableColumn<Report, LocalDateTime> generatedColumn;
    @FXML private TableColumn<Report, String> sizeColumn;
    
    // Progress and status
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Label progressLabel;
    
    // Summary statistics panel
    @FXML private Label totalReportsLabel;
    @FXML private Label completedReportsLabel;
    @FXML private Label pendingReportsLabel;
    @FXML private Label lastGeneratedLabel;
    
    // Report preview/details
    @FXML private TextArea reportPreviewArea;
    @FXML private ListView<String> keyInsightsList;
    @FXML private ListView<String> actionItemsList;
    @FXML private Label reportDetailsLabel;
    
    private ReportsViewModel viewModel;

    public void setViewModel(ReportsViewModel viewModel) {
        this.viewModel = viewModel;
        bindToViewModel();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupEventHandlers();
    }
    
    private void setupTableColumns() {
        // Configure table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        formatColumn.setCellValueFactory(new PropertyValueFactory<>("format"));
        generatedColumn.setCellValueFactory(new PropertyValueFactory<>("generatedDate"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedFileSize"));
        
        // Custom cell factories for better display
        typeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ReportType reportType, boolean empty) {
                super.updateItem(reportType, empty);
                if (empty || reportType == null) {
                    setText(null);
                } else {
                    setText(reportType.getDisplayName());
                }
            }
        });
        
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ReportStatus status, boolean empty) {
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
                        case FAILED:
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case GENERATING:
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        });
        
        generatedColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    setText(dateTime.format(formatter));
                }
            }
        });
        
        // Setup table selection listener
        reportsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (viewModel != null) {
                    viewModel.selectedReportProperty().set(newSelection);
                    updateReportDetails(newSelection);
                    updateButtonStates();
                }
            });
    }
    
    private void setupComboBoxes() {
        // Report Type ComboBox
        reportTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ReportType type) {
                return type != null ? type.getDisplayName() : "";
            }
            
            @Override
            public ReportType fromString(String string) {
                return ReportType.valueOf(string);
            }
        });
        
        // Report Format ComboBox
        formatCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ReportFormat format) {
                return format != null ? format.name() : "";
            }
            
            @Override
            public ReportFormat fromString(String string) {
                return ReportFormat.valueOf(string);
            }
        });
        
        // Status Filter ComboBox
        statusFilterCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(ReportStatus status) {
                return status != null ? status.name() : "All";
            }
            
            @Override
            public ReportStatus fromString(String string) {
                return "All".equals(string) ? null : ReportStatus.valueOf(string);
            }
        });
        
        // Add "All" option to status filter
        statusFilterCombo.getItems().add(null); // This represents "All"
        statusFilterCombo.setValue(null);
        
        // Set default dates
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
    }
    
    private void setupEventHandlers() {
        // Double-click on table row to view report
        reportsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Report selected = reportsTable.getSelectionModel().getSelectedItem();
                if (selected != null && viewModel != null) {
                    viewModel.getViewReportCommand().execute();
                }
            }
        });
    }
    
    private void bindToViewModel() {
        if (viewModel == null) return;
        
        // Bind collections to UI controls
        reportTypeCombo.setItems(viewModel.getAvailableReportTypes());
        formatCombo.setItems(viewModel.getAvailableFormats());
        templateCombo.setItems(viewModel.getAvailableTemplates());
        reportsTable.setItems(viewModel.getFilteredReports());
        statusFilterCombo.getItems().addAll(ReportStatus.values());
        
        // Bind form properties
        reportNameField.textProperty().bindBidirectional(viewModel.reportNameProperty());
        reportTypeCombo.valueProperty().bindBidirectional(viewModel.selectedReportTypeProperty());
        formatCombo.valueProperty().bindBidirectional(viewModel.selectedFormatProperty());
        startDatePicker.valueProperty().bindBidirectional(viewModel.startDateProperty());
        endDatePicker.valueProperty().bindBidirectional(viewModel.endDateProperty());
        templateCombo.valueProperty().bindBidirectional(viewModel.selectedTemplateProperty());
        
        // Bind filter properties
        filterField.textProperty().bindBidirectional(viewModel.filterTextProperty());
        statusFilterCombo.valueProperty().bindBidirectional(viewModel.filterStatusProperty());
        
        // Bind status properties
        progressBar.progressProperty().bind(viewModel.progressValueProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        progressLabel.visibleProperty().bind(viewModel.isGeneratingProperty());
        progressBar.visibleProperty().bind(viewModel.isGeneratingProperty());
        
        // Bind summary statistics
        totalReportsLabel.textProperty().bind(viewModel.totalReportsProperty());
        completedReportsLabel.textProperty().bind(viewModel.completedReportsProperty());
        pendingReportsLabel.textProperty().bind(viewModel.pendingReportsProperty());
        lastGeneratedLabel.textProperty().bind(viewModel.lastGeneratedDateProperty());
        
        // Setup button states
        updateButtonStates();
        
        // Setup callbacks
        viewModel.setOnViewReport(this::showReportViewer);
        viewModel.setOnShowMessage(this::showMessage);
    }
    
    // FXML Event Handlers
    @FXML
    private void handleGenerate() {
        if (viewModel != null) {
            viewModel.getGenerateReportCommand().execute();
            updateButtonStates();
        }
    }
    
    @FXML
    private void handleGenerateFromTemplate() {
        if (viewModel != null) {
            viewModel.getGenerateFromTemplateCommand().execute();
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
    private void handleDelete() {
        if (viewModel != null) {
            Report selected = viewModel.selectedReportProperty().get();
            if (selected != null) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Delete Report");
                confirmation.setHeaderText("Delete Report: " + selected.getName());
                confirmation.setContentText("Are you sure you want to delete this report? This action cannot be undone.");
                
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        viewModel.getDeleteReportCommand().execute();
                    }
                });
            }
        }
    }
    
    @FXML
    private void handleView() {
        if (viewModel != null) {
            viewModel.getViewReportCommand().execute();
        }
    }
    
    @FXML
    private void handleDownload() {
        if (viewModel != null) {
            viewModel.getDownloadReportCommand().execute();
        }
    }
    
    @FXML
    private void handleShare() {
        if (viewModel != null) {
            viewModel.getShareReportCommand().execute();
        }
    }
    
    @FXML
    private void handleSchedule() {
        if (viewModel != null) {
            viewModel.getScheduleReportCommand().execute();
        }
    }
    
    @FXML
    private void handleExport() {
        if (viewModel != null) {
            viewModel.getExportCommand().execute();
        }
    }
    
    @FXML
    private void handleClearFilters() {
        if (viewModel != null) {
            viewModel.getClearFiltersCommand().execute();
        }
    }
    
    // Helper methods
    private void updateReportDetails(Report report) {
        if (report == null) {
            reportPreviewArea.clear();
            keyInsightsList.getItems().clear();
            actionItemsList.getItems().clear();
            reportDetailsLabel.setText("No report selected");
            return;
        }
        
        // Update report details
        StringBuilder details = new StringBuilder();
        details.append("Report: ").append(report.getName()).append("\n");
        details.append("Type: ").append(report.getType().getDisplayName()).append("\n");
        details.append("Status: ").append(report.getStatus()).append("\n");
        details.append("Format: ").append(report.getFormat()).append("\n");
        if (report.getGeneratedDate() != null) {
            details.append("Generated: ").append(report.getGeneratedDate()).append("\n");
        }
        if (report.getDateRangeString() != null) {
            details.append("Period: ").append(report.getDateRangeString()).append("\n");
        }
        if (report.getFormattedFileSize() != null) {
            details.append("Size: ").append(report.getFormattedFileSize()).append("\n");
        }
        
        reportDetailsLabel.setText(details.toString());
        
        // Update content preview
        if (report.getContent() != null) {
            String preview = report.getContent();
            if (preview.length() > 500) {
                preview = preview.substring(0, 500) + "...";
            }
            reportPreviewArea.setText(preview);
        } else {
            reportPreviewArea.setText("No content preview available");
        }
        
        // Update insights and action items
        keyInsightsList.getItems().setAll(report.getKeyInsights());
        actionItemsList.getItems().setAll(report.getActionItems());
    }
    
    private void showReportViewer(Report report) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Report Viewer");
        dialog.setHeaderText("Report: " + report.getName());
        
        TextArea contentArea = new TextArea();
        contentArea.setText(report.getContent() != null ? report.getContent() : "No content available");
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(20);
        contentArea.setPrefColumnCount(60);
        
        dialog.getDialogPane().setContent(contentArea);
        dialog.getDialogPane().setPrefSize(800, 600);
        dialog.setResizable(true);
        
        dialog.showAndWait();
    }
    
    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Additional utility methods
    @FXML
    private void handleQuickFinancialSummary() {
        if (viewModel != null) {
            viewModel.selectedReportTypeProperty().set(ReportType.FINANCIAL_SUMMARY);
            viewModel.reportNameProperty().set("Financial Summary - " + LocalDate.now());
            handleGenerate();
        }
    }
    
    @FXML
    private void handleQuickBudgetAnalysis() {
        if (viewModel != null) {
            viewModel.selectedReportTypeProperty().set(ReportType.BUDGET_ANALYSIS);
            viewModel.reportNameProperty().set("Budget Analysis - " + LocalDate.now());
            handleGenerate();
        }
    }
    
    @FXML
    private void handleQuickGoalProgress() {
        if (viewModel != null) {
            viewModel.selectedReportTypeProperty().set(ReportType.GOAL_PROGRESS);
            viewModel.reportNameProperty().set("Goal Progress - " + LocalDate.now());
            handleGenerate();
        }
    }
    
    private void updateButtonStates() {
        if (viewModel != null) {
            generateButton.setDisable(!viewModel.getGenerateReportCommand().canExecute());
            templateButton.setDisable(!viewModel.getGenerateFromTemplateCommand().canExecute());
            deleteButton.setDisable(!viewModel.getDeleteReportCommand().canExecute());
            viewButton.setDisable(!viewModel.getViewReportCommand().canExecute());
            downloadButton.setDisable(!viewModel.getDownloadReportCommand().canExecute());
            shareButton.setDisable(!viewModel.getShareReportCommand().canExecute());
        }
    }
}