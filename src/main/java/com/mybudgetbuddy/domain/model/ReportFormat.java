package com.mybudgetbuddy.domain.model;

public enum ReportFormat {
    TEXT("Plain Text", "text/plain", ".txt"),
    PDF("PDF", "application/pdf", ".pdf"),
    HTML("HTML", "text/html", ".html"),
    EXCEL("Excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    CSV("CSV", "text/csv", ".csv"),
    JSON("JSON", "application/json", ".json"),
    WORD("Word", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");

    private final String displayName;
    private final String mimeType;
    private final String fileExtension;

    ReportFormat(String displayName, String mimeType, String fileExtension) {
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}