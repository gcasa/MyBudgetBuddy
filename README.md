# MyBudgetBuddy

A comprehensive personal finance management application built with JavaFX and SQLite, designed to help users track transactions, manage budgets, set financial goals, and generate insightful reports.

## 🌟 Features

### Core Functionality

- **Transaction Management**: Add, edit, and categorize income and expense transactions
- **Budget Planning**: Create and monitor various budget types (monthly, yearly, category-based)
- **Financial Goals**: Set and track progress toward financial objectives
- **Reporting**: Generate comprehensive financial reports and analytics
- **Categories**: Organize transactions with predefined and custom categories
- **Recurring Transactions**: Schedule automatic recurring payments and income
- **Multi-Plan Support**: Manage multiple financial planning scenarios

### User Interface

- Modern JavaFX-based desktop application
- Clean, intuitive MVVM (Model-View-ViewModel) architecture
- Responsive design with CSS styling
- Transaction entry and editing forms
- Financial dashboard with real-time insights

### Data Management

- SQLite database for persistent storage
- Automatic database initialization with default categories
- Data integrity with foreign key constraints
- Backup-friendly local database file

## 🏗️ Architecture

The application follows a clean architecture pattern with clear separation of concerns:

```
src/main/java/com/mybudgetbuddy/
├── App.java                           # JavaFX Application entry point
├── application/service/               # Business logic layer
│   ├── TransactionService.java        # Transaction operations interface
│   ├── CategoryService.java           # Category management interface
│   ├── BudgetService.java             # Budget management interface
│   └── impl/                          # Service implementations
├── controller/                        # JavaFX Controllers
├── viewmodel/                         # MVVM ViewModels
├── model/                             # Domain entities
├── infrastructure/database/           # Database layer
├── engine/                           # Business rule engines
└── presentation/view/                 # FXML view definitions
```

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **JavaFX 21** (automatically managed by Maven)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd CSC605-MyBudgetBuddy
```

### 2. Build the Project

```bash
mvn clean compile
```

### 3. Run the Application

```bash
mvn exec:java
```

Alternative JavaFX plugin method:

```bash
mvn javafx:run
```

### 4. Package the Application

```bash
mvn clean package
```

## 🔧 Build and Run Instructions

### Development Build

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package JAR
mvn package
```

### Running the Application

#### Option 1: Maven Exec Plugin (Recommended)

```bash
mvn exec:java
```

#### Option 2: JavaFX Maven Plugin

```bash
mvn javafx:run
```

#### Option 3: Direct Java Execution

```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml \
     --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
     -cp target/classes com.mybudgetbuddy.App
```

### Building on Windows

#### Prerequisites

1. **Install Java 17+**
   - Download the JDK from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
   - Run the installer and follow the prompts
   - Verify installation: `java -version`

2. **Install Maven 3.6+**
   - Download from [Maven Downloads](https://maven.apache.org/download.cgi) (choose the binary zip archive)
   - Extract to a directory, e.g., `C:\Program Files\Apache\maven`
   - Add `MAVEN_HOME` to environment variables and append `%MAVEN_HOME%\bin` to `PATH`
   - Verify installation: `mvn -version`

#### Build and Run (Command Prompt or PowerShell)

```cmd
:: Clone and navigate to the project
git clone <repository-url>
cd CSC605-MyBudgetBuddy

:: Build
mvn clean compile

:: Run
mvn exec:java

:: Or using the JavaFX plugin
mvn javafx:run

:: Package
mvn clean package
```

#### Notes for Windows

- Maven pulls JavaFX dependencies automatically — no manual JavaFX installation needed.
- The SQLite database is stored at `%USERPROFILE%\.mybudgetbuddy.db` (equivalent to `~/.mybudgetbuddy.db` on Unix).
- If you see `'mvn' is not recognized`, confirm that Maven's `bin` directory is on your `PATH` and restart your terminal.
- Windows Defender or antivirus software may slow the first build while downloading dependencies; this is normal.

### Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run specific test class
mvn test -Dtest=BudgetServiceTest
```

## 💾 Database Integration

### SQLite Database

- **Location**: `~/.mybudgetbuddy.db` (user home directory)
- **Auto-initialization**: Database schema created automatically on first run
- **Default Data**: Predefined income and expense categories

### Default Categories

**Income Categories:**

- Salary
- Freelance
- Investment

**Expense Categories:**

- Food & Dining
- Transportation
- Housing
- Entertainment
- Healthcare
- Shopping

### Database Schema

- `users` - User information
- `financial_plans` - Financial planning data
- `categories` - Transaction categories
- `transactions` - All transaction records
- `budgets` - Budget information
- `goals` - Financial goals
- `scenarios` - Planning scenarios
- `recommendations` - System recommendations
- `reports` - Generated reports

## 🧪 Testing

### Running Tests

```bash
# All tests
mvn test

# Integration tests only
java -cp target/classes:target/test-classes com.mybudgetbuddy.test.DatabaseIntegrationTest

# Specific test class
mvn test -Dtest=BudgetServiceTest
```

### Test Coverage

- Unit tests for service layer
- Integration tests for database operations
- Model validation tests

## 📁 Project Structure

```
MyBudgetBuddy/
├── pom.xml                            # Maven configuration
├── README.md                          # This file
├── SQLITE_INTEGRATION.md              # Database integration details
├── Documentation/
│   └── class-diagram.mmd              # Mermaid class diagrams
├── src/
│   ├── main/
│   │   ├── java/com/mybudgetbuddy/    # Java source code
│   │   └── resources/                 # FXML, CSS, and other resources
│   └── test/                          # Test classes
└── target/                            # Maven build output
```

## 🛠️ Technologies Used

### Core Technologies

- **Java 17**: Programming language
- **JavaFX 21**: Desktop UI framework
- **Maven**: Build and dependency management
- **SQLite 3.44**: Embedded database

### Key Dependencies

- **JavaFX Controls & FXML**: UI components and layouts
- **SQLite JDBC Driver**: Database connectivity
- **SLF4J**: Logging framework
- **JUnit 5**: Testing framework

### Architecture Patterns

- **MVVM**: Model-View-ViewModel pattern
- **Dependency Injection**: Service layer abstraction
- **Repository Pattern**: Data access abstraction
- **Command Pattern**: UI operations

## 📊 Key Models

### Transaction

- Comprehensive transaction tracking with categories, payment methods, and recurring support
- Links to budgets, goals, and financial plans
- Audit trail with creation and modification timestamps

### Budget

- Multiple budget types (monthly, yearly, category-based, project)
- Integration with transaction tracking
- Progress monitoring and alerts

### Goals

- Financial goal setting and progress tracking
- Priority levels and target dates
- Achievement monitoring

## 🔍 Development Notes

### Recent Fixes Applied

- ✅ Fixed compilation issues with escaped quotes in ViewModels
- ✅ Created missing BudgetType enum
- ✅ Implemented missing service layer methods
- ✅ Resolved Java enum structure requirements
- ✅ Fixed ambiguous import statements
- ✅ Added proper resource management

### Build Status

- **mvn clean compile**: ✅ SUCCESS
- **mvn clean package**: ✅ SUCCESS
- **Application execution**: ✅ WORKING

## 📝 Usage

1. **Launch the application** using one of the run methods above
2. **Add transactions** through the transaction entry form
3. **Set up budgets** to track spending in different categories
4. **Create financial goals** to work toward specific targets
5. **Generate reports** to analyze spending patterns and progress
6. **Review dashboard** for real-time financial insights

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is part of the CSC605 coursework assignment.

## 🚨 Troubleshooting

### Common Issues

- **JavaFX Module Issues**: Ensure Java 17+ and correct module path
- **Database Lock**: Close the application properly to release database locks  
- **Build Failures**: Run `mvn clean` before building
- **JavaFX Not Found**: Verify JavaFX is properly configured in your IDE

### Getting Help

- Check the [SQLITE_INTEGRATION.md](SQLITE_INTEGRATION.md) for database-specific issues
- Review error logs in the application output
- Verify all prerequisites are installed correctly
