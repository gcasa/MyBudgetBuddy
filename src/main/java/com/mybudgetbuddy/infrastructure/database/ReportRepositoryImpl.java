package com.mybudgetbuddy.infrastructure.database;

import com.mybudgetbuddy.domain.model.Report;
import com.mybudgetbuddy.domain.model.ReportFormat;
import com.mybudgetbuddy.domain.model.ReportStatus;
import com.mybudgetbuddy.domain.model.ReportType;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * SQLite implementation of ReportRepository
 */
public class ReportRepositoryImpl implements ReportRepository {
    
    private static final Logger LOGGER = Logger.getLogger(ReportRepositoryImpl.class.getName());
    private final DatabaseManager databaseManager;
    
    public ReportRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    @Override
    public Report save(Report report) {
        String sql = """
            INSERT OR REPLACE INTO reports (
                id, plan_id, user_id, name, description, type, format,
                start_date, end_date, included_categories, included_goals, included_budgets,
                include_graphs, include_recommendations, include_forecast,
                content, data_json, chart_urls, pdf_content, file_path,
                summary_stats, key_insights, action_items,
                status, generated_date, last_accessed_date, expiry_date,
                file_size_bytes, generated_by, page_count, template_version
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setReportParameters(stmt, report);
            stmt.executeUpdate();
            
            LOGGER.info("Report saved: " + report.getId());
            return report;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save report: " + report.getId(), e);
            throw new RuntimeException("Failed to save report", e);
        }
    }
    
    @Override
    public Optional<Report> findById(String reportId) {
        String sql = "SELECT * FROM reports WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, reportId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReport(rs));
                }
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find report by ID: " + reportId, e);
            throw new RuntimeException("Failed to find report", e);
        }
    }
    
    @Override
    public List<Report> findByPlanId(String planId) {
        String sql = "SELECT * FROM reports WHERE plan_id = ? ORDER BY generated_date DESC";
        return executeQuery(sql, planId);
    }
    
    @Override
    public List<Report> findByUserId(String userId) {
        String sql = "SELECT * FROM reports WHERE user_id = ? ORDER BY generated_date DESC";
        return executeQuery(sql, userId);
    }
    
    @Override
    public List<Report> findByStatus(ReportStatus status) {
        String sql = "SELECT * FROM reports WHERE status = ? ORDER BY generated_date DESC";
        return executeQuery(sql, status.name());
    }
    
    @Override
    public List<Report> findByType(ReportType type) {
        String sql = "SELECT * FROM reports WHERE type = ? ORDER BY generated_date DESC";
        return executeQuery(sql, type.name());
    }
    
    @Override
    public List<Report> findByGeneratedDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM reports WHERE DATE(generated_date) BETWEEN ? AND ? ORDER BY generated_date DESC";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());
            
            return executeQueryWithStatement(stmt);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find reports by date range", e);
            throw new RuntimeException("Failed to find reports by date range", e);
        }
    }
    
    @Override
    public List<Report> findExpiredReports() {
        String sql = "SELECT * FROM reports WHERE expiry_date < ?";
        return executeQuery(sql, LocalDateTime.now().toString());
    }
    
    @Override
    public List<Report> findLargeReports(long minSizeBytes) {
        String sql = "SELECT * FROM reports WHERE file_size_bytes >= ? ORDER BY file_size_bytes DESC";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, minSizeBytes);
            return executeQueryWithStatement(stmt);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find large reports", e);
            throw new RuntimeException("Failed to find large reports", e);
        }
    }
    
    @Override
    public List<Report> findScheduledReports(String planId) {
        String sql = "SELECT * FROM reports WHERE plan_id = ? AND status = 'PENDING' AND expiry_date > ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, planId);
            stmt.setString(2, LocalDateTime.now().toString());
            
            return executeQueryWithStatement(stmt);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find scheduled reports", e);
            throw new RuntimeException("Failed to find scheduled reports", e);
        }
    }
    
    @Override
    public boolean updateStatus(String reportId, ReportStatus status) {
        String sql = "UPDATE reports SET status = ? WHERE id = ?";
        return executeUpdate(sql, status.name(), reportId);
    }
    
    @Override
    public boolean updateFilePath(String reportId, String filePath, long fileSizeBytes) {
        String sql = "UPDATE reports SET file_path = ?, file_size_bytes = ?, status = 'COMPLETED' WHERE id = ?";
        return executeUpdate(sql, filePath, fileSizeBytes, reportId);
    }
    
    @Override
    public boolean deleteById(String reportId) {
        String sql = "DELETE FROM reports WHERE id = ?";
        return executeUpdate(sql, reportId);
    }
    
    @Override
    public int deleteOldReports(int daysOld) {
        String sql = "DELETE FROM reports WHERE generated_date < datetime('now', '-" + daysOld + " days')";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete old reports", e);
            throw new RuntimeException("Failed to delete old reports", e);
        }
    }
    
    @Override
    public long countByPlanId(String planId) {
        String sql = "SELECT COUNT(*) FROM reports WHERE plan_id = ?";
        return executeCount(sql, planId);
    }
    
    @Override
    public long countByUserId(String userId) {
        String sql = "SELECT COUNT(*) FROM reports WHERE user_id = ?";
        return executeCount(sql, userId);
    }
    
    @Override
    public long countByStatus(ReportStatus status) {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = ?";
        return executeCount(sql, status.name());
    }
    
    @Override
    public long getTotalSizeByPlanId(String planId) {
        String sql = "SELECT COALESCE(SUM(file_size_bytes), 0) FROM reports WHERE plan_id = ?";
        return executeCount(sql, planId);
    }
    
    // Helper methods
    
    private List<Report> executeQuery(String sql, Object... params) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return executeQueryWithStatement(stmt);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to execute query: " + sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }
    
    private List<Report> executeQueryWithStatement(PreparedStatement stmt) throws SQLException {
        List<Report> reports = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        }
        
        return reports;
    }
    
    private boolean executeUpdate(String sql, Object... params) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to execute update: " + sql, e);
            return false;
        }
    }
    
    private long executeCount(String sql, Object... params) {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to execute count: " + sql, e);
            return 0;
        }
    }
    
    private void setReportParameters(PreparedStatement stmt, Report report) throws SQLException {
        int index = 1;
        stmt.setString(index++, report.getId());
        stmt.setString(index++, report.getPlanId());
        stmt.setString(index++, report.getUserId());
        stmt.setString(index++, report.getName());
        stmt.setString(index++, report.getDescription());
        stmt.setString(index++, report.getType() != null ? report.getType().name() : null);
        stmt.setString(index++, report.getFormat() != null ? report.getFormat().name() : null);
        stmt.setString(index++, report.getStartDate() != null ? report.getStartDate().toString() : null);
        stmt.setString(index++, report.getEndDate() != null ? report.getEndDate().toString() : null);
        stmt.setString(index++, serializeStringList(report.getIncludedCategories()));
        stmt.setString(index++, serializeStringList(report.getIncludedGoals()));
        stmt.setString(index++, serializeStringList(report.getIncludedBudgets()));
        stmt.setBoolean(index++, report.isIncludeGraphs());
        stmt.setBoolean(index++, report.isIncludeRecommendations());
        stmt.setBoolean(index++, report.isIncludeForecast());
        stmt.setString(index++, report.getContent());
        stmt.setString(index++, serializeMap(report.getData()));
        stmt.setString(index++, serializeStringList(report.getChartUrls()));
        stmt.setBytes(index++, report.getPdfContent());
        stmt.setString(index++, report.getFilePath());
        stmt.setString(index++, serializeStringMap(report.getSummaryStats()));
        stmt.setString(index++, serializeStringList(report.getKeyInsights()));
        stmt.setString(index++, serializeStringList(report.getActionItems()));
        stmt.setString(index++, report.getStatus() != null ? report.getStatus().name() : null);
        stmt.setString(index++, report.getGeneratedDate() != null ? report.getGeneratedDate().toString() : null);
        stmt.setString(index++, report.getLastAccessedDate() != null ? report.getLastAccessedDate().toString() : null);
        stmt.setString(index++, report.getExpiryDate() != null ? report.getExpiryDate().toString() : null);
        stmt.setLong(index++, report.getFileSizeBytes());
        stmt.setString(index++, report.getGeneratedBy());
        stmt.setInt(index++, report.getPageCount());
        stmt.setString(index, report.getTemplateVersion());
    }
    
    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        
        report.setId(rs.getString("id"));
        report.setPlanId(rs.getString("plan_id"));
        report.setUserId(rs.getString("user_id"));
        report.setName(rs.getString("name"));
        report.setDescription(rs.getString("description"));
        
        String type = rs.getString("type");
        if (type != null) {
            report.setType(ReportType.valueOf(type));
        }
        
        String format = rs.getString("format");
        if (format != null) {
            report.setFormat(ReportFormat.valueOf(format));
        }
        
        String startDate = rs.getString("start_date");
        if (startDate != null) {
            report.setStartDate(LocalDate.parse(startDate));
        }
        
        String endDate = rs.getString("end_date");
        if (endDate != null) {
            report.setEndDate(LocalDate.parse(endDate));
        }
        
        report.setIncludedCategories(deserializeStringList(rs.getString("included_categories")));
        report.setIncludedGoals(deserializeStringList(rs.getString("included_goals")));
        report.setIncludedBudgets(deserializeStringList(rs.getString("included_budgets")));
        report.setIncludeGraphs(rs.getBoolean("include_graphs"));
        report.setIncludeRecommendations(rs.getBoolean("include_recommendations"));
        report.setIncludeForecast(rs.getBoolean("include_forecast"));
        report.setContent(rs.getString("content"));
        report.setData(deserializeMap(rs.getString("data_json")));
        report.setChartUrls(deserializeStringList(rs.getString("chart_urls")));
        report.setPdfContent(rs.getBytes("pdf_content"));
        report.setFilePath(rs.getString("file_path"));
        report.setSummaryStats(deserializeStringMap(rs.getString("summary_stats")));
        report.setKeyInsights(deserializeStringList(rs.getString("key_insights")));
        report.setActionItems(deserializeStringList(rs.getString("action_items")));
        
        String status = rs.getString("status");
        if (status != null) {
            report.setStatus(ReportStatus.valueOf(status));
        }
        
        String generatedDate = rs.getString("generated_date");
        if (generatedDate != null) {
            report.setGeneratedDate(LocalDateTime.parse(generatedDate));
        }
        
        String lastAccessedDate = rs.getString("last_accessed_date");
        if (lastAccessedDate != null) {
            report.setLastAccessedDate(LocalDateTime.parse(lastAccessedDate));
        }
        
        String expiryDate = rs.getString("expiry_date");
        if (expiryDate != null) {
            report.setExpiryDate(LocalDateTime.parse(expiryDate));
        }
        
        report.setFileSizeBytes(rs.getLong("file_size_bytes"));
        report.setGeneratedBy(rs.getString("generated_by"));
        report.setPageCount(rs.getInt("page_count"));
        report.setTemplateVersion(rs.getString("template_version"));
        
        return report;
    }
    
    private String serializeStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }
    
    private List<String> deserializeStringList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
    
    private String serializeStringMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(";"));
    }
    
    private Map<String, String> deserializeStringMap(String str) {
        Map<String, String> map = new HashMap<>();
        if (str == null || str.trim().isEmpty()) {
            return map;
        }
        
        Arrays.stream(str.split(";"))
                .filter(s -> !s.trim().isEmpty())
                .forEach(pair -> {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        map.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                });
        return map;
    }
    
    private String serializeMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        // Simple JSON-like serialization for basic types
        StringBuilder sb = new StringBuilder("{");
        map.entrySet().forEach(entry -> {
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue() != null ? entry.getValue().toString() : "null").append("\",");
        });
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1); // Remove last comma
        }
        sb.append("}");
        return sb.toString();
    }
    
    private Map<String, Object> deserializeMap(String str) {
        Map<String, Object> map = new HashMap<>();
        if (str == null || str.trim().isEmpty() || str.equals("{}")) {
            return map;
        }
        
        // Simple JSON-like deserialization
        String content = str.replace("{", "").replace("}", "");
        if (content.trim().isEmpty()) {
            return map;
        }
        
        Arrays.stream(content.split(","))
                .filter(s -> !s.trim().isEmpty())
                .forEach(pair -> {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replace("\"", "");
                        String value = keyValue[1].trim().replace("\"", "");
                        map.put(key, "null".equals(value) ? null : value);
                    }
                });
        return map;
    }
}