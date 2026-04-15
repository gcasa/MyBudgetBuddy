package com.mybudgetbuddy.infrastructure.database;

import com.mybudgetbuddy.domain.model.Report;
import com.mybudgetbuddy.domain.model.ReportStatus;
import com.mybudgetbuddy.domain.model.ReportType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Report entity data access operations.
 */
public interface ReportRepository {
    
    /**
     * Save a report to the database
     */
    Report save(Report report);
    
    /**
     * Find report by ID
     */
    Optional<Report> findById(String reportId);
    
    /**
     * Find all reports for a specific plan
     */
    List<Report> findByPlanId(String planId);
    
    /**
     * Find all reports for a specific user
     */
    List<Report> findByUserId(String userId);
    
    /**
     * Find reports by status
     */
    List<Report> findByStatus(ReportStatus status);
    
    /**
     * Find reports by type
     */
    List<Report> findByType(ReportType type);
    
    /**
     * Find reports generated within date range
     */
    List<Report> findByGeneratedDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find expired reports
     */
    List<Report> findExpiredReports();
    
    /**
     * Find reports larger than specified size
     */
    List<Report> findLargeReports(long minSizeBytes);
    
    /**
     * Find scheduled reports
     */
    List<Report> findScheduledReports(String planId);
    
    /**
     * Update report status
     */
    boolean updateStatus(String reportId, ReportStatus status);
    
    /**
     * Update report file path after generation
     */
    boolean updateFilePath(String reportId, String filePath, long fileSizeBytes);
    
    /**
     * Delete report by ID
     */
    boolean deleteById(String reportId);
    
    /**
     * Delete reports older than specified days
     */
    int deleteOldReports(int daysOld);
    
    /**
     * Count reports by plan
     */
    long countByPlanId(String planId);
    
    /**
     * Count reports by user
     */
    long countByUserId(String userId);
    
    /**
     * Count reports by status
     */
    long countByStatus(ReportStatus status);
    
    /**
     * Get total size of reports for a plan
     */
    long getTotalSizeByPlanId(String planId);
}