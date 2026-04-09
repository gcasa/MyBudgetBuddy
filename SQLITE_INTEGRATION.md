# SQLite Database Integration

This document describes the SQLite database integration added to MyBudgetBuddy.

## Overview

The application has been updated to use SQLite for persistent data storage instead of file serialization. This provides better performance, data integrity, and querying capabilities.

## Key Changes

### 1. Database Infrastructure

- **DatabaseManager**: Singleton class managing SQLite connection
- **DatabaseInitializer**: Creates database schema and default data
- **Database Location**: `~/.mybudgetbuddy.db` (user's home directory)

### 2. Service Layer Updates

- **TransactionServiceImpl**: Now uses SQLite for all CRUD operations
- **CategoryServiceImpl**: New service for managing categories with database persistence
- **Foreign Key Support**: Enabled for data integrity

### 3. Database Schema

#### Tables Created

- `users` - User information
- `financial_plans` - Financial planning data
- `categories` - Transaction categories (with default data)
- `transactions` - All transaction records
- `budgets` - Budget information
- `goals` - Financial goals
- `scenarios` - Planning scenarios
- `recommendations` - System recommendations
- `reports` - Generated reports

#### Default Categories

The system automatically creates default income and expense categories:

- **Income**: Salary, Freelance, Investment
- **Expense**: Food & Dining, Transportation, Housing, Entertainment, Healthcare, Shopping

### 4. Model Updates

- Added missing fields to `Transaction` model (accountId, parentTransactionId, nextOccurrence, endDate)
- Made enums public for package access (PaymentMethod, RecurringFrequency, TransactionStatus, CategoryType)
- Added compatibility methods for service layer integration

## Dependencies Added

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>
```

## Usage

### Automatic Initialization

The database is automatically initialized when the application starts. The `TransactionServiceImpl` constructor triggers schema creation if needed.

### Data Migration

Existing file-based data is not automatically migrated. Users will start with a clean database and default categories.

### Testing

Run the integration test to verify database functionality:

```bash
java -cp target/classes:target/test-classes com.mybudgetbuddy.test.DatabaseIntegrationTest
```

## Database Features

### Transactions

- All CRUD operations with SQL persistence
- Date range queries for reporting
- Category-based filtering
- Type-based filtering (Income, Expense, etc.)
- Financial calculations (totals, balances, category spending)

### Categories

- Predefined categories with colors and types
- Usage tracking (prevents deletion of used categories)
- Income/Expense categorization

### Data Integrity

- Foreign key constraints
- Automatic timestamps
- Transaction support
- Proper connection management

## Performance Benefits

1. **Faster Queries**: SQL-based filtering vs. in-memory stream operations
2. **Memory Efficiency**: Data loaded on-demand vs. keeping all in memory
3. **Concurrent Access**: SQLite handles concurrent reads/writes
4. **Data Integrity**: ACID compliance and foreign key constraints
5. **Flexible Reporting**: SQL aggregation functions for financial calculations

## Architecture Benefits

1. **Clean Separation**: Database logic isolated in infrastructure layer
2. **Service Layer**: Unchanged interfaces, only implementation updated
3. **MVVM Compatibility**: ViewModels work unchanged
4. **Testability**: Easy to test with in-memory SQLite database
5. **Scalability**: Can migrate to other SQL databases if needed

## Notes

- Database file is created automatically on first run
- Connection pooling not needed for SQLite (single connection)
- Database is closed cleanly when application shuts down
- Default categories are inserted only once
