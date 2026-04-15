package com.mybudgetbuddy.domain.model;

public enum ReportType {
    FINANCIAL_SUMMARY("Financial Summary", "Comprehensive overview of financial position"),
    BUDGET_ANALYSIS("Budget Analysis", "Analysis of budget performance vs actual spending"),
    GOAL_PROGRESS("Goal Progress", "Progress tracking towards financial goals"),
    CASH_FLOW("Cash Flow", "Income and expense flow over time"),
    EXPENSE_BREAKDOWN("Expense Breakdown", "Detailed breakdown of expenses by category"),
    INCOME_ANALYSIS("Income Analysis", "Analysis of income sources and trends"),
    TREND_ANALYSIS("Trend Analysis", "Financial trends and patterns over time"),
    FORECAST("Financial Forecast", "Projected financial outlook"),
    TAX_SUMMARY("Tax Summary", "Tax-related income and expense summary"),
    NET_WORTH("Net Worth", "Assets and liabilities summary"),
    CUSTOM("Custom Report", "User-defined custom report"),
    ANNUAL_SUMMARY("Annual Summary", "Year-end financial summary"),
    MONTHLY_SUMMARY("Monthly Summary", "Monthly financial overview"),
    COMPARATIVE_ANALYSIS("Comparative Analysis", "Period-to-period comparison report");

    private final String displayName;
    private final String description;

    ReportType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}