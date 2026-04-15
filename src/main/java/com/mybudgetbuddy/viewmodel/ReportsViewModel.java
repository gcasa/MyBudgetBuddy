package com.mybudgetbuddy.viewmodel;

import com.mybudgetbuddy.application.service.ReportService;
import com.mybudgetbuddy.command.Command;
import com.mybudgetbuddy.command.RelayCommand;
import com.mybudgetbuddy.domain.model.Report;
import com.mybudgetbuddy.domain.model.ReportFormat;
import com.mybudgetbuddy.domain.model.ReportStatus;
import com.mybudgetbuddy.domain.model.ReportType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Reports functionality with data binding and commands
 */
public class ReportsViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ReportsViewModel.class.getName());
    
    // Service dependencies
    private final ReportService reportService;
    
    // Observable properties for data binding
    private final ObservableList<Report> reports;
    private final ObservableList<Report> filteredReports;
    private final ObservableList<ReportType> availableReportTypes;
    private final ObservableList<ReportFormat> availableFormats;
    private final ObservableList<String> availableTemplates;
    
    // Selected items
    private final ObjectProperty<Report> selectedReport;
    private final ObjectProperty<ReportType> selectedReportType;
    private final ObjectProperty<ReportFormat> selectedFormat;
    private final StringProperty selectedTemplate;
    
    // Form properties for report generation
    private final StringProperty reportName;
    private final StringProperty planId;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;
    private final StringProperty filterText;
    private final ObjectProperty<ReportStatus> filterStatus;
    
    // Status properties
    private final BooleanProperty isLoading;
    private final BooleanProperty isGenerating;
    private final StringProperty statusMessage;
    private final DoubleProperty progressValue;
    
    // Summary statistics
    private final StringProperty totalReports;
    private final StringProperty completedReports;
    private final StringProperty pendingReports;
    private final StringProperty lastGeneratedDate;
    
    // Commands
    private final Command generateReportCommand;
    private final Command generateFromTemplateCommand;
    private final Command refreshCommand;
    private final Command deleteReportCommand;
    private final Command viewReportCommand;
    private final Command downloadReportCommand;
    private final Command shareReportCommand;
    private final Command scheduleReportCommand;
    private final Command exportCommand;
    private final Command clearFiltersCommand;
    
    // Callbacks for UI interactions
    private Consumer<Report> onViewReport;
    private Consumer<Report> onEditReport;
    private Consumer<String> onShowMessage;
    private Runnable onReportGenerated;
    
    public ReportsViewModel(ReportService reportService) {
        if (reportService == null) {
            throw new IllegalArgumentException("ReportService cannot be null");
        }
        
        this.reportService = reportService;
        
        // Initialize collections
        this.reports = FXCollections.observableArrayList();
        this.filteredReports = FXCollections.observableArrayList();
        this.availableReportTypes = FXCollections.observableArrayList(ReportType.values());
        this.availableFormats = FXCollections.observableArrayList(ReportFormat.values());
        this.availableTemplates = FXCollections.observableArrayList();
        
        // Initialize properties
        this.selectedReport = new SimpleObjectProperty<>();
        this.selectedReportType = new SimpleObjectProperty<>(ReportType.FINANCIAL_SUMMARY);
        this.selectedFormat = new SimpleObjectProperty<>(ReportFormat.PDF);
        this.selectedTemplate = new SimpleStringProperty();
        
        this.reportName = new SimpleStringProperty();
        this.planId = new SimpleStringProperty(null);
        this.startDate = new SimpleObjectProperty<>(LocalDate.now().minusDays(30));
        this.endDate = new SimpleObjectProperty<>(LocalDate.now());
        this.filterText = new SimpleStringProperty();
        this.filterStatus = new SimpleObjectProperty<>();
        
        this.isLoading = new SimpleBooleanProperty();
        this.isGenerating = new SimpleBooleanProperty();
        this.statusMessage = new SimpleStringProperty("Ready");
        this.progressValue = new SimpleDoubleProperty(0);
        
        this.totalReports = new SimpleStringProperty("0");
        this.completedReports = new SimpleStringProperty("0");
        this.pendingReports = new SimpleStringProperty("0"); 
        this.lastGeneratedDate = new SimpleStringProperty("Never");
        
        // Initialize commands
        this.generateReportCommand = new RelayCommand(this::handleGenerateReport, this::canGenerateReport);
        this.generateFromTemplateCommand = new RelayCommand(this::handleGenerateFromTemplate, this::canGenerateFromTemplate);
        this.refreshCommand = new RelayCommand(this::handleRefresh);
        this.deleteReportCommand = new RelayCommand(this::handleDeleteReport, this::canDeleteReport);
        this.viewReportCommand = new RelayCommand(this::handleViewReport, this::canViewReport);
        this.downloadReportCommand = new RelayCommand(this::handleDownloadReport, this::canDownloadReport);
        this.shareReportCommand = new RelayCommand(this::handleShareReport, this::canShareReport);
        this.scheduleReportCommand = new RelayCommand(this::handleScheduleReport, this::canScheduleReport);
        this.exportCommand = new RelayCommand(this::handleExport, this::canExport);
        this.clearFiltersCommand = new RelayCommand(this::handleClearFilters);
        
        // Setup property listeners
        setupPropertyListeners();
        
        // Load initial data
        loadInitialData();
    }
    
    // Public getters for properties
    public ObservableList<Report> getReports() { return reports; }
    public ObservableList<Report> getFilteredReports() { return filteredReports; }
    public ObservableList<ReportType> getAvailableReportTypes() { return availableReportTypes; }
    public ObservableList<ReportFormat> getAvailableFormats() { return availableFormats; }
    public ObservableList<String> getAvailableTemplates() { return availableTemplates; }
    
    public ObjectProperty<Report> selectedReportProperty() { return selectedReport; }
    public ObjectProperty<ReportType> selectedReportTypeProperty() { return selectedReportType; }
    public ObjectProperty<ReportFormat> selectedFormatProperty() { return selectedFormat; }
    public StringProperty selectedTemplateProperty() { return selectedTemplate; }
    
    public StringProperty reportNameProperty() { return reportName; }
    public StringProperty planIdProperty() { return planId; }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }
    public StringProperty filterTextProperty() { return filterText; }
    public ObjectProperty<ReportStatus> filterStatusProperty() { return filterStatus; }
    
    public BooleanProperty isLoadingProperty() { return isLoading; }
    public BooleanProperty isGeneratingProperty() { return isGenerating; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public DoubleProperty progressValueProperty() { return progressValue; }
    
    public StringProperty totalReportsProperty() { return totalReports; }
    public StringProperty completedReportsProperty() { return completedReports; }
    public StringProperty pendingReportsProperty() { return pendingReports; }
    public StringProperty lastGeneratedDateProperty() { return lastGeneratedDate; }
    
    // Command getters
    public Command getGenerateReportCommand() { return generateReportCommand; }
    public Command getGenerateFromTemplateCommand() { return generateFromTemplateCommand; }
    public Command getRefreshCommand() { return refreshCommand; }
    public Command getDeleteReportCommand() { return deleteReportCommand; }
    public Command getViewReportCommand() { return viewReportCommand; }
    public Command getDownloadReportCommand() { return downloadReportCommand; }
    public Command getShareReportCommand() { return shareReportCommand; }
    public Command getScheduleReportCommand() { return scheduleReportCommand; }
    public Command getExportCommand() { return exportCommand; }
    public Command getClearFiltersCommand() { return clearFiltersCommand; }
    
    // Callback setters
    public void setOnViewReport(Consumer<Report> onViewReport) { this.onViewReport = onViewReport; }
    public void setOnEditReport(Consumer<Report> onEditReport) { this.onEditReport = onEditReport; }
    public void setOnShowMessage(Consumer<String> onShowMessage) { this.onShowMessage = onShowMessage; }
    public void setOnReportGenerated(Runnable onReportGenerated) { this.onReportGenerated = onReportGenerated; }
    
    // Command implementations
    private void handleGenerateReport() {
        generateReportAsync();
    }
    
    private void handleGenerateFromTemplate() {
        if (selectedTemplate.get() == null || selectedTemplate.get().isEmpty()) {
            showMessage("Please select a template");
            return;
        }
        generateFromTemplateAsync();
    }
    
    private void handleRefresh() {
        loadReportsAsync();
    }
    
    private void handleDeleteReport() {
        Report report = selectedReport.get();
        if (report != null) {
            deleteReportAsync(report);
        }
    }
    
    private void handleViewReport() {
        Report report = selectedReport.get();
        if (report != null && onViewReport != null) {
            onViewReport.accept(report);
        }
    }
    
    private void handleDownloadReport() {
        Report report = selectedReport.get();
        if (report != null) {
            downloadReportAsync(report);
        }
    }
    
    private void handleShareReport() {
        Report report = selectedReport.get();
        if (report != null) {
            String shareLink = reportService.generateShareableLink(report.getId());
            showMessage("Share link generated: " + shareLink);
        }
    }
    
    private void handleScheduleReport() {
        Report report = selectedReport.get();
        if (report != null) {
            showMessage("Schedule report feature coming soon!");
        }
    }
    
    private void handleExport() {
        Report report = selectedReport.get();
        if (report != null) {
            exportReportAsync(report);
        }
    }
    
    private void handleClearFilters() {
        filterText.set("");
        filterStatus.set(null);
        applyFilters();
    }
    
    // Command can-execute methods
    private boolean canGenerateReport() {
        return !isGenerating.get() && selectedReportType.get() != null && 
               reportName.get() != null && !reportName.get().trim().isEmpty();
    }
    
    private boolean canGenerateFromTemplate() {
        return !isGenerating.get() && selectedTemplate.get() != null && !selectedTemplate.get().isEmpty();
    }
    
    private boolean canDeleteReport() {
        return selectedReport.get() != null && !isLoading.get();
    }
    
    private boolean canViewReport() {
        Report report = selectedReport.get();
        return report != null && report.getStatus() == ReportStatus.COMPLETED;
    }
    
    private boolean canDownloadReport() {
        Report report = selectedReport.get();
        return report != null && report.getStatus() == ReportStatus.COMPLETED && 
               report.getFilePath() != null;
    }
    
    private boolean canShareReport() {
        Report report = selectedReport.get();
        return report != null && report.getStatus() == ReportStatus.COMPLETED;
    }
    
    private boolean canScheduleReport() {
        return selectedReport.get() != null;
    }
    
    private boolean canExport() {
        Report report = selectedReport.get();
        return report != null && report.getStatus() == ReportStatus.COMPLETED;
    }
    
    // Async operations
    private void generateReportAsync() {
        Task<Report> task = new Task<>() {
            @Override
            protected Report call() throws Exception {
                updateMessage("Generating report...");
                updateProgress(0.1, 1.0);
                
                Report report = reportService.generateReport(
                    reportName.get(),
                    selectedReportType.get(),
                    selectedFormat.get(),
                    planId.get(),
                    startDate.get(),
                    endDate.get()
                );
                
                updateProgress(1.0, 1.0);
                return report;
            }
            
            @Override
            protected void succeeded() {
                Report report = getValue();
                reports.add(0, report);
                applyFilters();
                updateSummaryStats();
                showMessage("Report generated successfully: " + report.getName());
                if (onReportGenerated != null) {
                    onReportGenerated.run();
                }
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                LOGGER.log(Level.SEVERE, "Failed to generate report", exception);
                showMessage("Failed to generate report: " + exception.getMessage());
            }
        };
        
        bindTaskToUI(task);
        new Thread(task).start();
    }
    
    private void generateFromTemplateAsync() {
        Task<Report> task = new Task<>() {
            @Override
            protected Report call() throws Exception {
                updateMessage("Generating report from template...");
                updateProgress(0.1, 1.0);
                
                Report report = reportService.generateReportFromTemplate(
                    selectedTemplate.get(),
                    planId.get(),
                    java.util.Map.of(
                        "startDate", startDate.get().toString(),
                        "endDate", endDate.get().toString()
                    )
                );
                
                updateProgress(1.0, 1.0);
                return report;
            }
            
            @Override
            protected void succeeded() {
                Report report = getValue();
                reports.add(0, report);
                applyFilters();
                updateSummaryStats();
                showMessage("Report generated from template: " + report.getName());
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                LOGGER.log(Level.SEVERE, "Failed to generate report from template", exception);
                showMessage("Failed to generate report: " + exception.getMessage());
            }
        };
        
        bindTaskToUI(task);
        new Thread(task).start();
    }
    
    private void loadReportsAsync() {
        Task<List<Report>> task = new Task<>() {
            @Override
            protected List<Report> call() throws Exception {
                updateMessage("Loading reports...");
                return reportService.getReportsByPlanId(planId.get());
            }
            
            @Override
            protected void succeeded() {
                List<Report> loadedReports = getValue();
                reports.setAll(loadedReports);
                applyFilters();
                updateSummaryStats();
                statusMessage.set("Loaded " + loadedReports.size() + " reports");
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                LOGGER.log(Level.SEVERE, "Failed to load reports", exception);
                showMessage("Failed to load reports: " + exception.getMessage());
            }
        };
        
        bindTaskToUI(task);
        new Thread(task).start();
    }
    
    private void deleteReportAsync(Report report) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Deleting report...");
                reportService.deleteReport(report.getId());
                return null;
            }
            
            @Override
            protected void succeeded() {
                reports.remove(report);
                applyFilters();
                updateSummaryStats();
                selectedReport.set(null);
                showMessage("Report deleted successfully");
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                LOGGER.log(Level.SEVERE, "Failed to delete report", exception);
                showMessage("Failed to delete report: " + exception.getMessage());
            }
        };
        
        bindTaskToUI(task);
        new Thread(task).start();
    }
    
    private void downloadReportAsync(Report report) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Downloading report...");
                String downloadPath = System.getProperty("user.home") + "/Downloads/" + report.getName() + report.getFormat().getFileExtension();
                reportService.downloadReport(report.getId(), downloadPath);
                return null;
            }
            
            @Override
            protected void succeeded() {
                showMessage("Report downloaded successfully");
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                LOGGER.log(Level.SEVERE, "Failed to download report", exception);
                showMessage("Failed to download report: " + exception.getMessage());
            }
        };
        
        bindTaskToUI(task);
        new Thread(task).start();
    }
    
    private void exportReportAsync(Report report) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Exporting report...");
                return reportService.exportReportAsJson(report.getId());
            }
            
            @Override
            protected void succeeded() {
                String content = getValue();
                showMessage("Report exported. Content length: " + content.length() + " characters");
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                LOGGER.log(Level.SEVERE, "Failed to export report", exception);
                showMessage("Failed to export report: " + exception.getMessage());
            }
        };
        
        bindTaskToUI(task);
        new Thread(task).start();
    }
    
    // Helper methods
    private void setupPropertyListeners() {
        // Listen for filter changes
        filterText.addListener((obs, oldVal, newVal) -> applyFilters());
        filterStatus.addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Update report name when type changes
        selectedReportType.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (reportName.get() == null || reportName.get().isEmpty())) {
                reportName.set(newVal.getDisplayName() + " - " + LocalDate.now());
            }
        });
    }
    
    private void loadInitialData() {
        // Load available templates
        List<String> templates = reportService.getAvailableReportTemplates();
        availableTemplates.setAll(templates);
        
        // Load existing reports
        loadReportsAsync();
    }
    
    private void applyFilters() {
        filteredReports.clear();
        
        String filterTextLower = filterText.get() != null ? filterText.get().toLowerCase() : "";
        ReportStatus statusFilter = filterStatus.get();
        
        for (Report report : reports) {
            boolean matches = true;
            
            // Text filter
            if (!filterTextLower.isEmpty()) {
                matches = report.getName().toLowerCase().contains(filterTextLower) ||
                         report.getType().getDisplayName().toLowerCase().contains(filterTextLower);
            }
            
            // Status filter
            if (matches && statusFilter != null) {
                matches = report.getStatus() == statusFilter;
            }
            
            if (matches) {
                filteredReports.add(report);
            }
        }
    }
    
    private void updateSummaryStats() {
        totalReports.set(String.valueOf(reports.size()));
        
        long completed = reports.stream().filter(r -> r.getStatus() == ReportStatus.COMPLETED).count();
        completedReports.set(String.valueOf(completed));
        
        long pending = reports.stream().filter(r -> r.getStatus() == ReportStatus.PENDING || 
                                                   r.getStatus() == ReportStatus.GENERATING).count();
        pendingReports.set(String.valueOf(pending));
        
        Optional<Report> lastReport = reports.stream()
            .filter(r -> r.getGeneratedDate() != null)
            .max((r1, r2) -> r1.getGeneratedDate().compareTo(r2.getGeneratedDate()));
        
        if (lastReport.isPresent()) {
            lastGeneratedDate.set(lastReport.get().getGeneratedDate().toString());
        } else {
            lastGeneratedDate.set("Never");
        }
    }
    
    private void bindTaskToUI(Task<?> task) {
        isGenerating.bind(task.runningProperty());
        statusMessage.bind(task.messageProperty());
        progressValue.bind(task.progressProperty());
        
        task.setOnSucceeded(e -> {
            isGenerating.unbind();
            statusMessage.unbind();
            progressValue.unbind();
            
            isGenerating.set(false);
            progressValue.set(0);
        });
        
        task.setOnFailed(e -> {
            isGenerating.unbind();
            statusMessage.unbind(); 
            progressValue.unbind();
            
            isGenerating.set(false);
            progressValue.set(0);
            statusMessage.set("Ready");
        });
    }
    
    private void showMessage(String message) {
        if (onShowMessage != null) {
            onShowMessage.accept(message);
        }
        statusMessage.set(message);
    }
}