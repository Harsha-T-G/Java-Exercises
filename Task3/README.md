# Bank Account Management System

This is a simple console-based Bank Account Management System implemented in Java 17/21 using Maven and JUnit 5. No Spring Boot or database is used.

## Project Structure

- `src/main/java`: Contains the Java source code.
- `src/test/java`: Contains the JUnit 5 test classes.

## How to Build and Run

### Prerequisites
- Java JDK 17 or 21
- Maven 3.6+

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Run the Application
```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

## Design Overview

Please refer to `CLAUDE.md` for detailed explanation of the design techniques used, including encapsulation, abstraction, immutability, exception handling, and separation of responsibilities.

## Recent Improvements (Based on Code Review)

1. **Separated Business Logic from Model**: 
   - The `Account` class is now a pure data container (anemic model) with no business logic.
   - All business logic for account operations (deposit, withdraw, block, activate) resides in the `AccountServices` class.
   - This addresses the review comment: "Data Logic shouldn't be defined in the model classes".

2. **Reduced `System.out.println` Calls**:
   - In `Main.java`, we use `StringBuilder` to build menu and message strings before printing, reducing I/O operations.
   - This addresses the review comment about excessive `System.out.println` usage.

3. **Encapsulated Exception Handling**:
   - Created a `BankingOperations` facade that catches exceptions from the service layer and returns user-friendly messages.
   - This keeps the `Main` class free of try/catch blocks, addressing the review comment about moving try/catch blocks to the service layer.

4. **Enhanced Test Documentation**:
   - Added descriptive Javadoc comments to both `AccountTest.java` and `AccountServicesTest.java`.
   - Structured tests with clear Arrange/Act/Assert methodology in comments.
   - Separated test concerns: `AccountTest` focuses on the account as a data container, while `AccountServicesTest` tests all business logic operations.
   - This addresses the review comment about adding JUnit comments to test cases.

## System Architecture

The system now follows a layered architecture:

- **Account (Model)**: Pure data container representing account information
- **Transaction (Model)**: Immutable record of financial transactions  
- **AccountServices (Service Layer)**: Contains all business logic for account operations
- **BankingOperations (Facade)**: Handles user interactions and exception translation
- **Main (Presentation)**: Handles console I/O only

This separation ensures that each component has a single responsibility, making the system more maintainable, testable, and aligned with object-oriented principles.

## How It Works

1. **Account Creation**: Call `BankingOperations.createAccount()` which delegates to `AccountServices.createAccount()`
2. **Deposit/Withdrawal**: Call `BankingOperations.deposit()` or `BankingOperations.withdraw()` which delegate to `AccountServices` 
3. **Account Status Changes**: Call `BankingOperations.blockAccount()` or `BankingOperations.activateAccount()` 
4. **Balance Inquiry**: Call `BankingOperations.checkBalance()`
5. **Transaction History**: Call `BankingOperations.getTransactionHistory()`

The `AccountServices` class enforces all business rules:
- Prevents negative initial balances
- Ensures account number uniqueness
- Validates transaction amounts (must be positive)
- Checks sufficient funds for withdrawals
- Blocks transactions on frozen accounts
- Maintains transaction history with proper timestamps and balance tracking

## Running the Application

When you run the application, you'll see a text-based menu allowing you to:
- Create accounts with optional initial balance
- Deposit and withdraw funds
- Block and unblock accounts
- Check current balances
- View detailed transaction history

All operations proper validation and error handling through the service layer.

This implementation satisfies all requirements while demonstrating clean architecture principles and separation of concerns.