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
- The `BankAccount` class encapsulates the account data (account number, holder name, balance, status, and transaction history) as private fields.
- Public methods (deposit, withdraw, block, activate, getBalance, getTransactionHistory) provide controlled access to these fields.
- The balance field has no public setter; it can only be modified through deposit and withdrawal operations that validate business rules.
- The transaction history is kept as an unmodifiable list when accessed externally to prevent direct modification.

### Abstraction
- We abstract the concept of a bank account into a `BankAccount` class that hides the complexity of transaction management and state changes.
- The `Transaction` class abstracts a single transaction with its properties (ID, type, amount, timestamps, balances).
- Custom exceptions (e.g., `InsufficientBalanceException`, `AccountBlockedException`, `InvalidAmountException`, `DuplicateAccountNumberException`) abstract specific error conditions.

### Classes and Objects
- **BankAccount**: Represents a single bank account with all its attributes and behaviors.
- **Transaction**: Represents a financial transaction (deposit or withdrawal) with immutable properties.
- **BankAccountService** (or similar): Manages a collection of bank accounts, ensuring uniqueness of account numbers and providing operations like creating accounts, finding accounts, etc.
- **Main**: Contains the main method to run the application (if a console interface is implemented) or can be used for demonstration.

### Immutability
- The `Transaction` class is designed to be immutable: once created, its fields cannot be changed. This ensures transaction integrity.
- The `BankAccount` class ensures that the account number is immutable after creation (set only in the constructor).
- The transaction history is stored as a list that, when returned to callers, is unmodifiable (using `Collections.unmodifiableList`).

### Exception Handling
- We use meaningful custom exceptions to communicate specific business rule violations:
  - `InvalidAmountException` for non-positive deposit/withdrawal amounts.
  - `InsufficientBalanceException` for withdrawal exceeding balance.
  - `AccountBlockedException` for attempting transactions on a blocked account.
  - `DuplicateAccountNumberException` for attempting to create an account with an existing number.
- These exceptions are checked exceptions, forcing the caller to handle them appropriately.
- Failed operations (due to exceptions) leave the account state unchanged, ensuring atomicity.

### Separation of Responsibilities
- **BankAccount**: Responsible for managing its own state (balance, status) and transaction history.
- **Transaction**: Solely responsible for representing a transaction record.
- **BankAccountService** (or AccountManager): Responsible for managing the collection of accounts (e.g., creating new accounts, finding accounts by number, ensuring uniqueness).
- **Main** or **UI Layer**: Handles user interaction (if present) or orchestrates the workflow (in a simple demo, this might be in the main method or test classes).
- This separation ensures that each class has a single reason to change, making the system more maintainable and testable.

### Testing Strategy
- **Unit Testing Focus**: Tests focus on individual units (Account, Transaction, AccountServices) in isolation using JUnit 5.
- **Test Coverage**: Aim for high coverage of business logic, especially boundary conditions and error cases.
- **Test Organization**: 
  - One test class per main service/model class (e.g., AccountServicesTest, AccountTest)
  - Test methods follow naming convention: `testMethodUnderTest_ExpectedBehavior_StateUnderTest`
  - Use `@BeforeEach` to set up fresh test instances for each test
- **Key Test Areas**:
  1. **Positive Path Tests**: Verify successful operations (deposit, withdrawal, account creation)
  2. **Negative Path Tests**: Verify proper exception handling for invalid inputs
  3. **Boundary Condition Tests**: Test edge cases like zero amounts, maximum values
  4. **State Transition Tests**: Verify account status changes (block/activate) affect behavior correctly
  5. **Immutability Tests**: Verify that returned collections cannot modify internal state
  6. **Exception Tests**: Use `assertThrows` to verify correct exceptions are thrown
  7. **Data Integrity Tests**: Verify balance and transaction history are correctly updated
- **Assertion Strategy**:
  - Use `assertEquals` for value comparisons (including BigDecimal.compareTo() == 0)
  - Use `assertTrue`/`assertFalse` for boolean conditions
  - Use `assertThrows` for exception testing
  - Use `assertNotNull`/`assertNull` for null checks
- **Test Data**: Use meaningful constants for test data (account numbers, names, amounts) to improve readability

### Additional Design Considerations
- **Use of BigDecimal**: For precise monetary calculations, avoiding floating-point inaccuracies.
- **Transaction History**: Stored as a list of Transaction objects, providing an audit trail.
- **Account Status**: Simple enum (ACTIVE/BLOCKED) to control transaction permissions.
- **Uniqueness of Account Number**: Enforced by the service layer when creating new accounts.
