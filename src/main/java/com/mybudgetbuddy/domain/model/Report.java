package com.mybudgetbuddy.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String planId;
    private String userId;
    private String name;
    private String description;
    private ReportType type;
    private ReportFormat format;
    
    // Report parameters
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> includedCategories;
    private List<String> includedGoals;
    private List<String> includedBudgets;
    private boolean includeGraphs;
    private boolean includeRecommendations;
    private boolean includeForecast;
    
    // Report content (generated data)
    private String content; // HTML, JSON, or other format
    private Map<String, Object> data; // Structured data for the report
    private List<String> chartUrls; // URLs to generated charts
    private byte[] pdfContent; // For PDF reports
    private String filePath; // Where the report file is saved
    
    // Summary statistics
    private Map<String, String> summaryStats; // Key metrics
    private List<String> keyInsights; // Generated insights
    private List<String> actionItems; // Recommended actions
    
    // Metadata
    private ReportStatus status;
    private LocalDateTime generatedDate;
    private LocalDateTime lastAccessedDate;
    private LocalDateTime expiryDate;
    private long fileSizeBytes;
    private String generatedBy;
    private int pageCount; // For PDF reports
    private String templateVersion;
    
    public Report() {
        this.id = UUID.randomUUID().toString();
        this.includedCategories = new ArrayList<>();
        this.includedGoals = new ArrayList<>();
        this.includedBudgets = new ArrayList<>();
        this.data = new HashMap<>();
        this.chartUrls = new ArrayList<>();
        this.summaryStats = new HashMap<>();
        this.keyInsights = new ArrayList<>();
        this.actionItems = new ArrayList<>();
        this.status = ReportStatus.PENDING;
        this.generatedDate = LocalDateTime.now();
        this.includeGraphs = true;
        this.includeRecommendations = true;
        this.includeForecast = false;
        this.format = ReportFormat.TEXT;
    }
    
    public Report(String name, ReportType type, LocalDate startDate, LocalDate endDate) {
        this();
        this.name = name;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Business methods
    public void markAsGenerated(String filePath) {
        this.status = ReportStatus.COMPLETED;
        this.filePath = filePath;
        this.generatedDate = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        addActionItem("Error generating report: " + errorMessage);
    }
    
    public void markAsAccessed() {
        this.lastAccessedDate = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
    
    public boolean isReady() {
        return status == ReportStatus.COMPLETED && filePath != null;
    }
    
    public void addSummaryStats(String key, String value) {
        summaryStats.put(key, value);
    }
    
    public void addKeyInsight(String insight) {
        keyInsights.add(insight);
    }
    
    public void addActionItem(String actionItem) {
        actionItems.add(actionItem);
    }
    
    public void addChartUrl(String chartUrl) {
        chartUrls.add(chartUrl);
    }
    
    public void addCategory(String categoryId) {
        if (!includedCategories.contains(categoryId)) {
            includedCategories.add(categoryId);
        }
    }
    
    public void addGoal(String goalId) {
        if (!includedGoals.contains(goalId)) {
            includedGoals.add(goalId);
        }
    }
    
    public void addBudget(String budgetId) {
        if (!includedBudgets.contains(budgetId)) {
            includedBudgets.add(budgetId);
        }
    }
    
    public long getDaysOld() {
        return generatedDate.toLocalDate().until(LocalDate.now()).getDays();
    }
    
    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return (fileSizeBytes / 1024) + " KB";
        } else {
            return (fileSizeBytes / (1024 * 1024)) + " MB";
        }
    }
    
    public String getDateRangeString() {
        if (startDate != null && endDate != null) {
            return startDate.toString() + " to " + endDate.toString();
        } else if (startDate != null) {
            return "From " + startDate.toString();
        } else if (endDate != null) {
            return "Until " + endDate.toString();
        }
        return "All time";
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ReportType getType() { return type; }
    public void setType(ReportType type) { this.type = type; }
    
    public ReportFormat getFormat() { return format; }
    public void setFormat(ReportFormat format) { this.format = format; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public List<String> getIncludedCategories() { return new ArrayList<>(includedCategories); }
    public void setIncludedCategories(List<String> includedCategories) { this.includedCategories = new ArrayList<>(includedCategories); }
    
    public List<String> getIncludedGoals() { return new ArrayList<>(includedGoals); }
    public void setIncludedGoals(List<String> includedGoals) { this.includedGoals = new ArrayList<>(includedGoals); }
    
    public List<String> getIncludedBudgets() { return new ArrayList<>(includedBudgets); }
    public void setIncludedBudgets(List<String> includedBudgets) { this.includedBudgets = new ArrayList<>(includedBudgets); }
    
    public boolean isIncludeGraphs() { return includeGraphs; }
    public void setIncludeGraphs(boolean includeGraphs) { this.includeGraphs = includeGraphs; }
    
    public boolean isIncludeRecommendations() { return includeRecommendations; }
    public void setIncludeRecommendations(boolean includeRecommendations) { this.includeRecommendations = includeRecommendations; }
    
    public boolean isIncludeForecast() { return includeForecast; }
    public void setIncludeForecast(boolean includeForecast) { this.includeForecast = includeForecast; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Map<String, Object> getData() { return new HashMap<>(data); }
    public void setData(Map<String, Object> data) { this.data = new HashMap<>(data); }
    
    public List<String> getChartUrls() { return new ArrayList<>(chartUrls); }
    public void setChartUrls(List<String> chartUrls) { this.chartUrls = new ArrayList<>(chartUrls); }
    
    public byte[] getPdfContent() { return pdfContent != null ? pdfContent.clone() : null; }
    public void setPdfContent(byte[] pdfContent) { this.pdfContent = pdfContent != null ? pdfContent.clone() : null; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public Map<String, String> getSummaryStats() { return new HashMap<>(summaryStats); }
    public void setSummaryStats(Map<String, String> summaryStats) { this.summaryStats = new HashMap<>(summaryStats); }
    
    public List<String> getKeyInsights() { return new ArrayList<>(keyInsights); }
    public void setKeyInsights(List<String> keyInsights) { this.keyInsights = new ArrayList<>(keyInsights); }
    
    public List<String> getActionItems() { return new ArrayList<>(actionItems); }
    public void setActionItems(List<String> actionItems) { this.actionItems = new ArrayList<>(actionItems); }
    
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    
    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }
    
    public LocalDateTime getLastAccessedDate() { return lastAccessedDate; }
    public void setLastAccessedDate(LocalDateTime lastAccessedDate) { this.lastAccessedDate = lastAccessedDate; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    
    public String getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(String templateVersion) { this.templateVersion = templateVersion; }
}