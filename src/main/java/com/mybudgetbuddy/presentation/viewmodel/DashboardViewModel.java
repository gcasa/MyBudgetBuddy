package com.mybudgetbuddy.presentation.viewmodel;

import com.mybudgetbuddy.application.service.*;
import com.mybudgetbuddy.domain.model.*;
import com.mybudgetbuddy.command.Command;
import com.mybudgetbuddy.command.RelayCommand;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class DashboardViewModel {
    
    // Services
    private final PlanService planService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private final ReportService reportService;
    
    // Properties
    private final StringProperty currentPlanName;
    private final StringProperty totalIncome;
    private final StringProperty totalExpenses;
    private final StringProperty netIncome;
    private final StringProperty totalSavings;
    private final StringProperty emergencyFund;
    private final StringProperty netWorth;
    
    // Collections
    private final ObservableList<Transaction> recentTransactions;
    private final ObservableList<Budget> budgetSummary;
    private final ObservableList<Goal> topGoals;
    private final ObservableList<Recommendation> recommendations;
    
    // Chart data
    private final ObjectProperty<ObservableList<Object>> incomeVsExpensesData;
    private final ObjectProperty<ObservableList<Object>> budgetProgressData;
    private final ObjectProperty<ObservableList<Object>> goalProgressData;
    private final ObjectProperty<ObservableList<Object>> spendingByCategoryData;
    
    // Commands
    private final Command refreshDashboardCommand;
    private final Command viewAllTransactionsCommand;
    private final Command viewAllBudgetsCommand;
    private final Command viewAllGoalsCommand;
    private final Command generateReportCommand;
    
    // Navigation callbacks
    private Consumer<String> onNavigateToTransactions;
    private Consumer<String> onNavigateToBudgets;
    private Consumer<String> onNavigateToGoals;
    private Consumer<String> onNavigateToReports;
    
    // Current context
    private String currentPlanId;
    private String currentUserId;
    private LocalDate dashboardDate;
    
    public DashboardViewModel(PlanService planService, TransactionService transactionService,
                             BudgetService budgetService, GoalService goalService, 
                             ReportService reportService) {
        this.planService = planService;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.goalService = goalService;
        this.reportService = reportService;
        
        // Initialize properties
        this.currentPlanName = new SimpleStringProperty(\"No Plan Selected\");
        this.totalIncome = new SimpleStringProperty(\"$0.00\");
        this.totalExpenses = new SimpleStringProperty(\"$0.00\");
        this.netIncome = new SimpleStringProperty(\"$0.00\");
        this.totalSavings = new SimpleStringProperty(\"$0.00\");
        this.emergencyFund = new SimpleStringProperty(\"$0.00\");
        this.netWorth = new SimpleStringProperty(\"$0.00\");
        
        // Initialize collections
        this.recentTransactions = FXCollections.observableArrayList();
        this.budgetSummary = FXCollections.observableArrayList();
        this.topGoals = FXCollections.observableArrayList();
        this.recommendations = FXCollections.observableArrayList();
        
        // Initialize chart data
        this.incomeVsExpensesData = new SimpleObjectProperty<>(FXCollections.observableArrayList());
        this.budgetProgressData = new SimpleObjectProperty<>(FXCollections.observableArrayList());
        this.goalProgressData = new SimpleObjectProperty<>(FXCollections.observableArrayList());
        this.spendingByCategoryData = new SimpleObjectProperty<>(FXCollections.observableArrayList());
        
        // Initialize commands
        this.refreshDashboardCommand = new RelayCommand(this::loadDashboard);
        this.viewAllTransactionsCommand = new RelayCommand(this::handleViewAllTransactions);
        this.viewAllBudgetsCommand = new RelayCommand(this::handleViewAllBudgets);
        this.viewAllGoalsCommand = new RelayCommand(this::handleViewAllGoals);
        this.generateReportCommand = new RelayCommand(this::handleGenerateReport);
        
        this.dashboardDate = LocalDate.now();
    }
    
    // Public methods
    public void loadDashboard(String planId, String userId) {
        this.currentPlanId = planId;
        this.currentUserId = userId;
        loadDashboard();
    }
    
    public void loadDashboard() {
        if (currentPlanId == null) return;
        
        loadPlanInfo();
        loadFinancialSummary();
        loadRecentTransactions();
        loadBudgetSummary();
        loadTopGoals();
        loadRecommendations();
        loadChartData();
    }
    
    private void loadPlanInfo() {
        planService.getPlanById(currentPlanId).ifPresent(plan -> {
            currentPlanName.set(plan.getName());
        });
    }
    
    private void loadFinancialSummary() {
        LocalDate monthStart = dashboardDate.withDayOfMonth(1);
        LocalDate monthEnd = dashboardDate.withDayOfMonth(dashboardDate.lengthOfMonth());
        
        BigDecimal income = transactionService.getTotalIncome(currentPlanId, monthStart, monthEnd);
        BigDecimal expenses = transactionService.getTotalExpenses(currentPlanId, monthStart, monthEnd);
        BigDecimal net = income.subtract(expenses);
        
        totalIncome.set(String.format(\"$%,.2f\", income));
        totalExpenses.set(String.format(\"$%,.2f\", expenses));
        netIncome.set(String.format(\"$%,.2f\", net));
        
        // TODO: Calculate savings, emergency fund, net worth from appropriate services
        totalSavings.set(\"$0.00\"); // Placeholder
        emergencyFund.set(\"$0.00\"); // Placeholder
        netWorth.set(\"$0.00\"); // Placeholder
    }
    
    private void loadRecentTransactions() {
        List<Transaction> recent = transactionService.getRecentTransactions(currentPlanId, 5);
        recentTransactions.setAll(recent);
    }
    
    private void loadBudgetSummary() {
        List<Budget> budgets = budgetService.getActiveBudgets(currentPlanId);
        budgetSummary.setAll(budgets);
    }
    
    private void loadTopGoals() {
        List<Goal> goals = goalService.getActiveGoals(currentPlanId);
        // Limit to top 3 goals by priority
        List<Goal> topThree = goals.stream()
            .sorted((g1, g2) -> g2.getPriority().getLevel() - g1.getPriority().getLevel())
            .limit(3)
            .toList();
        topGoals.setAll(topThree);
    }
    
    private void loadRecommendations() {
        // TODO: Load from recommendation engine
        recommendations.clear();
    }
    
    private void loadChartData() {
        // TODO: Implement chart data loading
        // This would involve creating appropriate data structures for JavaFX charts
    }
    
    // Command handlers
    private void handleViewAllTransactions() {
        if (onNavigateToTransactions != null) {
            onNavigateToTransactions.accept(currentPlanId);
        }
    }
    
    private void handleViewAllBudgets() {
        if (onNavigateToBudgets != null) {
            onNavigateToBudgets.accept(currentPlanId);
        }
    }
    
    private void handleViewAllGoals() {
        if (onNavigateToGoals != null) {
            onNavigateToGoals.accept(currentPlanId);
        }
    }
    
    private void handleGenerateReport() {
        if (onNavigateToReports != null) {
            onNavigateToReports.accept(currentPlanId);
        }
    }
    
    // Property getters
    public StringProperty currentPlanNameProperty() { return currentPlanName; }
    public StringProperty totalIncomeProperty() { return totalIncome; }
    public StringProperty totalExpensesProperty() { return totalExpenses; }
    public StringProperty netIncomeProperty() { return netIncome; }
    public StringProperty totalSavingsProperty() { return totalSavings; }
    public StringProperty emergencyFundProperty() { return emergencyFund; }
    public StringProperty netWorthProperty() { return netWorth; }
    
    // Collection getters
    public ObservableList<Transaction> getRecentTransactions() { return recentTransactions; }
    public ObservableList<Budget> getBudgetSummary() { return budgetSummary; }
    public ObservableList<Goal> getTopGoals() { return topGoals; }
    public ObservableList<Recommendation> getRecommendations() { return recommendations; }
    
    // Chart data getters
    public ObjectProperty<ObservableList<Object>> incomeVsExpensesDataProperty() { return incomeVsExpensesData; }
    public ObjectProperty<ObservableList<Object>> budgetProgressDataProperty() { return budgetProgressData; }
    public ObjectProperty<ObservableList<Object>> goalProgressDataProperty() { return goalProgressData; }
    public ObjectProperty<ObservableList<Object>> spendingByCategoryDataProperty() { return spendingByCategoryData; }
    
    // Command getters
    public Command getRefreshDashboardCommand() { return refreshDashboardCommand; }
    public Command getViewAllTransactionsCommand() { return viewAllTransactionsCommand; }
    public Command getViewAllBudgetsCommand() { return viewAllBudgetsCommand; }
    public Command getViewAllGoalsCommand() { return viewAllGoalsCommand; }
    public Command getGenerateReportCommand() { return generateReportCommand; }
    
    // Navigation setters
    public void setOnNavigateToTransactions(Consumer<String> onNavigateToTransactions) {
        this.onNavigateToTransactions = onNavigateToTransactions;
    }
    
    public void setOnNavigateToBudgets(Consumer<String> onNavigateToBudgets) {
        this.onNavigateToBudgets = onNavigateToBudgets;
    }
    
    public void setOnNavigateToGoals(Consumer<String> onNavigateToGoals) {
        this.onNavigateToGoals = onNavigateToGoals;
    }
    
    public void setOnNavigateToReports(Consumer<String> onNavigateToReports) {
        this.onNavigateToReports = onNavigateToReports;
    }
}