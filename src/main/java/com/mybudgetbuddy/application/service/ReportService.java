package com.mybudgetbuddy.application.service;

import com.mybudgetbuddy.domain.model.Report;
import com.mybudgetbuddy.domain.model.ReportType;
import com.mybudgetbuddy.domain.model.ReportFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReportService {
    
    // Report generation
    Report generateReport(String name, ReportType type, ReportFormat format, String planId, LocalDate startDate, LocalDate endDate);
    Report generateCustomReport(String name, Map<String, Object> parameters, String planId);
    void regenerateReport(String reportId);
    
    // Report management
    Optional<Report> getReportById(String reportId);
    List<Report> getReportsByPlanId(String planId);
    List<Report> getReportsByUserId(String userId);
    void deleteReport(String reportId);
    void archiveReport(String reportId);
    
    // Report templates
    List<String> getAvailableReportTemplates();
    Report generateReportFromTemplate(String templateName, String planId, Map<String, Object> parameters);
    void saveReportAsTemplate(String reportId, String templateName);
    
    // Scheduled reports
    void scheduleReport(Report report, String cronExpression);
    void cancelScheduledReport(String reportId);
    List<Report> getScheduledReports(String planId);
    void processScheduledReports();
    
    // Report export
    byte[] exportReportAsPdf(String reportId);
    String exportReportAsHtml(String reportId);
    byte[] exportReportAsExcel(String reportId);
    String exportReportAsJson(String reportId);
    
    // Report sharing
    String generateShareableLink(String reportId);
    void shareReportViaEmail(String reportId, List<String> emailAddresses);
    void shareReportViaNotification(String reportId, String userId);
    
    // Report analytics
    Map<String, Object> getReportAnalytics(String reportId);
    List<String> getKeyInsights(String reportId);
    List<String> getActionItems(String reportId);
    void addUserFeedback(String reportId, String feedback, int rating);
    
    // Predefined reports
    Report generateFinancialSummary(String planId, LocalDate startDate, LocalDate endDate);
    Report generateBudgetAnalysis(String planId, LocalDate startDate, LocalDate endDate);
    Report generateGoalProgressReport(String planId);
    Report generateCashFlowReport(String planId, LocalDate startDate, LocalDate endDate);
    Report generateExpenseBreakdown(String planId, LocalDate startDate, LocalDate endDate);
    Report generateIncomeAnalysis(String planId, LocalDate startDate, LocalDate endDate);
    Report generateTrendAnalysis(String planId, int months);
    Report generateNetWorthReport(String planId, LocalDate asOfDate);
    Report generateAnnualSummary(String planId, int year);
    
    // Comparative reports
    Report generatePeriodComparison(String planId, LocalDate period1Start, LocalDate period1End, 
                                   LocalDate period2Start, LocalDate period2End);
    Report generateBudgetVsActualReport(String planId, LocalDate startDate, LocalDate endDate);
    Report generateGoalComparisonReport(String planId, List<String> goalIds);
    
    // Report formatting
    String formatReportContent(Report report, ReportFormat targetFormat);
    void addChartToReport(String reportId, String chartType, Map<String, Object> chartData);
    void addTableToReport(String reportId, String tableTitle, List<Map<String, Object>> tableData);
    
    // Report delivery
    void emailReport(String reportId, String emailAddress);
    void downloadReport(String reportId, String downloadPath);
    void printReport(String reportId);
    
    // Report maintenance
    void cleanupExpiredReports();
    void optimizeReportStorage();
    List<Report> findLargeReports(long minSizeBytes);
    void compressReport(String reportId);
}