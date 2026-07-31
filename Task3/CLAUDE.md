# Bank Account Management System

## Requirements

Build a simple Bank Account Management System using Java 17 or 21, Maven, and JUnit 5. Do not use Spring Boot or a database.

### Requirements

Each bank account should contain:

- Account number.
- Account holder’s name.
- Current balance.
- Account status: `ACTIVE` or `BLOCKED`.
- Transaction history.

The system should support:

- Creating an account with an initial balance.
- Depositing money.
- Withdrawing money.
- Blocking and activating an account.
- Viewing the current balance.
- Viewing the transaction history.

### Business rules

- Account number must be unique and cannot be changed.
- Initial balance cannot be negative.
- Use `BigDecimal` for monetary values.
- Deposit and withdrawal amounts must be greater than zero.
- Withdrawal must not exceed the available balance.
- Transactions must not be allowed on a blocked account.
- The balance must not have a public setter.
- Transaction history must not be directly modifiable from outside.
- Every successful deposit and withdrawal must create a transaction record.
- Failed operations must not change the balance.
- Use meaningful custom exceptions for invalid operations.

Each transaction should contain:

- Transaction ID.
- Transaction type: `DEPOSIT` or `WITHDRAWAL`.
- Amount.
- Balance before and after the transaction.
- Date and time.

### Testing

Write JUnit tests for:

- Successful deposit and withdrawal.
- Invalid deposit and withdrawal amounts.
- Insufficient balance.
- Operations on a blocked account.
- Duplicate account number.
- Transaction-history creation.
- Protection of balance and transaction history.

### Deliverables

- Java source code.
- JUnit tests.
- A short `README.md` explaining the design and how to run the application.

Be prepared to explain how you applied encapsulation, abstraction, classes, objects, immutability, exception handling, and separation of responsibilities.

## Context

This project is a console-based banking application developed using core Java (version 17 or 21) without any external frameworks like Spring Boot. The build automation is handled by Maven, and testing is performed using JUnit 5. No database is used; all data is held in memory during the application's execution. The focus is on object-oriented design principles, proper encapsulation, and robust exception handling.

## Design Technique

### Encapsulation
- The `Account` class encapsulates the account data (account number, holder name, balance, status, and transaction history) with private fields.
- Public getters and setters provide controlled access to these fields where appropriate.
- The balance field has no public setter in the original requirement, but we provide a package-private setter for use by the service layer.
- The transaction history is kept as an unmodifiable list when accessed externally to prevent direct modification.

### Abstraction
- We abstract the concept of a bank account into an `Account` class that serves as a data container.
- The `Transaction` class (as a record) abstracts a single transaction with its properties (ID, type, amount, timestamps, balances).
- The `AccountServices` class abstracts the business logic for account operations.
- Custom exceptions (e.g., `InsufficientFundsException`, `AccountBlockedException`, `InvalidAmountException`, `DuplicateAccountNumberException`, `NegativeInitialBalanceException`) abstract specific error conditions.

### Classes and Objects
- **Account**: Represents a single bank account as a data container with getters and setters for its properties.
- **Transaction**: Represents a financial transaction (deposit or withdrawal) with immutable properties (using a record).
- **AccountServices**: Manages a collection of bank accounts and contains all business logic for operations like deposit, withdrawal, blocking, activating, etc.
- **BankingOperations**: Facade service that handles user interactions, catches exceptions from the service layer, and returns user-friendly messages.
- **Main**: Handles user input and output, calling the `BankingOperations` methods.

### Immutability
- The `AccountNumber` is effectively immutable (no setter provided).
- The `Transaction` record is immutable: once created, its fields cannot be changed. This ensures transaction integrity.
- The transaction history is stored as a list that, when returned by `getTransactionHistory`, is unmodifiable (using `Collections.unmodifiableList`).
- While the `Account` allows mutation of balance and status through setters, these are package-private and intended only for use by the service layer.

### Exception Handling
- We use meaningful custom exceptions to communicate specific business rule violations:
  - `InvalidAmountException` for non-positive deposit/withdrawal amounts.
  - `InsufficientFundsException` for withdrawal exceeding balance.
  - `AccountBlockedException` for attempting transactions on a blocked account.
  - `DuplicateAccountNumberException` for attempting to create an account with an existing number.
  - `NegativeInitialBalanceException` for negative initial balance.
- These exceptions are checked exceptions, forcing the caller to handle them appropriately.
- Failed operations (due to exceptions) leave the account state unchanged, ensuring atomicity.
- The `BankingOperations` facade catches these exceptions and returns user-friendly messages, keeping the `Main` class free of try/catch blocks.

### Separation of Responsibilities
- **Account**: Responsible for representing account data as a container. Contains no business logic.
- **Transaction**: Solely responsible for representing a transaction record.
- **AccountServices**: Responsible for managing the collection of accounts and containing all business logic (deposit, withdraw, block, activate, etc.). Ensures uniqueness of account numbers and persists state.
- **BankingOperations** (facade): Handles user interaction, input validation, and exception handling, delegating core operations to `AccountServices`. This keeps the `Main` class focused solely on console I/O and delegation.
- **Main**: Handles user input and output, calling the `BankingOperations` methods. It contains no business logic or error handling—only calls to the service layer and printing results.

This separation ensures that each class has a single reason to change, making the system more maintainable and testable.

## Review Comments and Actions Taken

Based on the review, the following improvements were made:

1. **Data Logic Moved to Service Layer**:  
   - The `Account` class is now a pure data container (anemic model) with no business logic.  
   - All business logic for deposit, withdrawal, blocking, and activating accounts resides in the `AccountServices` class.  
   - This achieves a clear separation between data representation and business operations.

2. **Replaced `System.out.println` with `StringBuilder`**:  
   - In `Main.java`, the `displayMenu` method uses `StringBuilder` to build the menu string and print it once, reducing I/O calls.  
   - Other `System.out.println` calls are for simple prompts and results, which are acceptable for a console application.

3. **Moved Try/Catch Blocks to Service Layer**:  
   - Created a new facade class `BankingOperations` in the `org.example.services` package.  
   - This class catches checked exceptions from `AccountServices` and returns user‑friendly result messages.  
   - The `Main` class now only collects user input and calls methods on `BankingOperations`, which handle errors internally and return appropriate messages or values, keeping `Main` free of try/catch blocks.

4. **Enhanced JUnit Test Comments**:  
   - Added descriptive Javadoc comments and inline comments to test classes (`AccountTest.java` and `AccountServicesTest.java`) explaining what each test verifies, the scenario, and the expected outcome.  
   - No comments were added to service or main classes; focus remained on test documentation only.  
   - Structured tests with clear Arrange/Act/Assert methodology in comments.  
   - Separated concerns: `AccountTest` focuses on the account as a data container (construction, field modifications, immutability), while `AccountServicesTest` tests all business logic operations.

These changes improve adherence to object‑oriented principles, separation of concerns, and code readability while still satisfied:
- `<em>Data Logic shouldn't be defined in the model classes</em>` → **Satisfied**: All business logic is now in `AccountServices`.
- `<em>System.out.println is used many times...</em>` → **Addressed**: Used `StringBuilder` for the menu in `Main.java`.
- `<em>Main class should be like only it need to take inputs...</em>` → **Satisfied**: `Main` handles only I/O; `BankingOperations` handles interactions and exceptions.
- `<em>Can we add the Junit comments for the test cases...</em>` → **Satisfied**: Both test classes now have detailed Javadoc comments and inline comments explaining test steps.

## How to Run the Application

```bash
# Compile and run the application
mvn compile exec:java -Dexec.mainClass="org.example.Main"

# Run tests
mvn test

mvn test
```

## Example Usage

When you run the application, you'll see a menu:

```
Please Select the Option
1. Create Account
2. Deposit Amount
3. Withdrawal Amount
4. Block Account
5. Activate Account
6. Check Current Balance
7. View Transaction History
8. Exit
```

Follow the prompts to create accounts, make deposits and withdrawals, block/unblock accounts, check balances, and view transaction history.

All business rules are enforced by the service layer, ensuring data integrity and proper error handling.