package org.example.services;

import org.example.exception.*;
import org.example.models.Account;
import org.example.models.Transaction;
import org.example.constants.AccountStatus;
import org.example.constants.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AccountServices class.
 * Tests cover account creation, deposits, withdrawals, blocking/unblocking,
 * balance checks, and verification of immutability and business rules.
 */
class AccountServicesTest {

    private AccountServices accountServices;
    private static final long TEST_ACCOUNT_NUMBER = 123456789L;
    private static final String TEST_ACCOUNT_HOLDER = "John Doe";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @BeforeEach
    void setUp() {
        // Initialize a fresh AccountServices instance before each test
        accountServices = new AccountServices();
    }

    /**
     * Tests that an account can be created with an initial balance,
     * verifies account details, and checks that an initial deposit transaction is recorded.
     */
    @Test
    void testCreateAccountWithInitialBalance() throws Exception {
        // Arrange (implicit in setUp and method parameters)

        // Act
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber(), "Account number should match");
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName(), "Account holder name should match");
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Initial balance should match");
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus(), "Account should be active by default");

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(1, transactions.size(), "Should have exactly one transaction (initial deposit)");

        Transaction transaction = transactions.get(0);
        assertEquals(TransactionType.DEPOSIT, transaction.type(), "First transaction should be a deposit");
        assertEquals(INITIAL_BALANCE, transaction.amount(), "Deposit amount should equal initial balance");
        assertEquals(BigDecimal.ZERO, transaction.balanceBefore(), "Balance before deposit should be zero");
        assertEquals(INITIAL_BALANCE, transaction.balanceAfter(), "Balance after deposit should equal initial balance");
    }

    /**
     * Tests that an account can be created with zero initial balance
     * and that no transaction is recorded when the initial balance is zero.
     */
    @Test
    void testCreateAccountWithoutInitialBalance() throws Exception {
        // Arrange

        // Act
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(BigDecimal.ZERO, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(0, transactions.size(), "Should have no transactions when initial balance is zero");
    }

    /**
     * Ensures that creating an account with a negative initial balance
     * throws the appropriate exception and that the account is not persisted.
     */
    @Test
    void testCreateAccountWithNegativeInitialBalance() {
        // Arrange (implicit in parameters)

        // Act & Assert
        assertThrows(NegativeInitialBalanceException.class, () -> {
            accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, new BigDecimal("-100.00"));
        });

        assertFalse(accountServices.accountExists(TEST_ACCOUNT_NUMBER),
                "Account should not exist after failed creation");
    }

    /**
     * Tests that creating an account with a duplicate account number
     * throws a DuplicateAccountNumberException.
     */
    @Test
    void testCreateDuplicateAccountNumber() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(DuplicateAccountNumberException.class, () -> {
            accountServices.createAccount(TEST_ACCOUNT_NUMBER, "Jane Doe", INITIAL_BALANCE);
        });
    }

    /**
     * Tests a successful deposit operation:
     * - Verifies that the balance increases by the deposit amount.
     * - Ensures a new deposit transaction is recorded.
     */
    @Test
    void testSuccessfulDeposit() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal depositAmount = new BigDecimal("500.00");

        // Act
        BigDecimal newBalance = accountServices.deposit(TEST_ACCOUNT_NUMBER, depositAmount);
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(INITIAL_BALANCE.add(depositAmount), newBalance);
        assertEquals(INITIAL_BALANCE.add(depositAmount), account.getCurrentBalance());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size(), "Should have initial deposit + new deposit");

        Transaction depositTransaction = transactions.get(1); // Most recent transaction
        assertEquals(TransactionType.DEPOSIT, depositTransaction.type());
        assertEquals(depositAmount, depositTransaction.amount());
        assertEquals(INITIAL_BALANCE, depositTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE.add(depositAmount), depositTransaction.balanceAfter());
    }

    /**
     * Tests that depositing a zero amount throws an InvalidAmountException
     * and leaves the account balance unchanged.
     */
    @Test
    void testDepositWithZeroAmount() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountServices.deposit(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed deposit");
    }

    /**
     * Tests that depositing a negative amount throws an InvalidAmountException
     * and leaves the account balance unchanged.
     */
    @Test
    void testDepositWithNegativeAmount() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("-50.00"));
        });
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed deposit");
    }

    /**
     * Tests that depositing to a blocked account throws an AccountBlockedException
     * and leaves the account balance unchanged.
     */
    @Test
    void testDepositOnBlockedAccount() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountServices.blockAccount(TEST_ACCOUNT_NUMBER);

        // Act & Assert
        assertThrows(AccountBlockedException.class, () -> {
            accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged when account is blocked");
    }

    /**
     * Tests a successful withdrawal operation:
     * - Verifies that the balance decreases by the withdrawal amount.
     * - Ensures a new withdrawal transaction is recorded.
     */
    @Test
    void testSuccessfulWithdrawal() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal withdrawalAmount = new BigDecimal("300.00");

        // Act
        BigDecimal newBalance = accountServices.withdraw(TEST_ACCOUNT_NUMBER, withdrawalAmount);
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), newBalance);
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), account.getCurrentBalance());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size(), "Should have initial deposit + withdrawal");

        Transaction withdrawalTransaction = transactions.get(1); // Most recent transaction
        assertEquals(TransactionType.WITHDRAWAL, withdrawalTransaction.type());
        assertEquals(withdrawalAmount, withdrawalTransaction.amount());
        assertEquals(INITIAL_BALANCE, withdrawalTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), withdrawalTransaction.balanceAfter());
    }

    /**
     * Tests that attempting to withdraw more than the available balance
     * throws an InsufficientFundsException and leaves the balance unchanged.
     */
    @Test
    void testWithdrawalWithInsufficientFunds() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal withdrawalAmount = INITIAL_BALANCE.add(new BigDecimal("100.00")); // More than balance

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, withdrawalAmount);
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed withdrawal");
    }

    /**
     * Tests that withdrawing a zero amount throws an InvalidAmountException
     * and leaves the account balance unchanged.
     */
    @Test
    void testWithdrawalWithZeroAmount() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed withdrawal");
    }

    /**
     * Tests that withdrawing a negative amount throws an InvalidAmountException
     * and leaves the account balance unchanged.
     */
    @Test
    void testWithdrawalWithNegativeAmount() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("-50.00"));
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed withdrawal");
    }

    /**
     * Tests that withdrawing from a blocked account throws an AccountBlockedException
     * and leaves the account balance unchanged.
     */
    @Test
    void testWithdrawalOnBlockedAccount() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountServices.blockAccount(TEST_ACCOUNT_NUMBER);

        // Act & Assert
        assertThrows(AccountBlockedException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged when account is blocked");
    }

    /**
     * Tests that blocking and then activating an account toggles its status correctly.
     */
    @Test
    void testBlockAndActivateAccount() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertEquals(AccountStatus.ACTIVE, accountServices.getAccountStatus(TEST_ACCOUNT_NUMBER));

        accountServices.blockAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.BLOCKED, accountServices.getAccountStatus(TEST_ACCOUNT_NUMBER),
                "Account should be blocked after blockAccount()");

        accountServices.activateAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.ACTIVE, accountServices.getAccountStatus(TEST_ACCOUNT_NUMBER),
                "Account should be active after activateAccount()");

        accountServices.blockAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.BLOCKED, accountServices.getAccountStatus(TEST_ACCOUNT_NUMBER),
                "Blocking already blocked account should remain blocked");

        accountServices.activateAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.ACTIVE, accountServices.getAccountStatus(TEST_ACCOUNT_NUMBER),
                "Activating already active account should remain active");
    }

    /**
     * Tests that getBalance returns the correct balance for an existing account.
     */
    @Test
    void testGetBalance() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act
        BigDecimal balance = accountServices.getBalance(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(INITIAL_BALANCE, balance);
    }

    /**
     * Tests that getAccountDetails returns the correct account information.
     */
    @Test
    void testGetAccountDetails() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    /**
     * Tests that accountExists correctly reports whether an account exists.
     */
    @Test
    void testAccountExists() throws Exception {
        // Assert initial state
        assertFalse(accountServices.accountExists(TEST_ACCOUNT_NUMBER));

        // Act
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Assert
        assertTrue(accountServices.accountExists(TEST_ACCOUNT_NUMBER));

        // Assert non-existing account
        assertFalse(accountServices.accountExists(999999999L));
    }

    /**
     * Tests that the transaction history returned by getAccountDetails is immutable.
     */
    @Test
    void testTransactionHistoryIsUnmodifiable() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("50.00"));

        // Act
        List<Transaction> history = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER).getTransactionHistory();
        int originalSize = history.size();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, history::clear);
        assertEquals(originalSize, accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER).getTransactionHistory().size(),
                "Original transaction history should remain unmodified");
    }

    /**
     * Tests that the balance can only be changed through deposit and withdrawal operations.
     */
    @Test
    void testBalanceCanOnlyBeChangedThroughDepositWithdrawal() throws Exception {
        // Arrange
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);

        BigDecimal originalBalance = account.getCurrentBalance();

        // Act
        accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        assertNotEquals(originalBalance, account.getCurrentBalance(), "Balance should change after deposit");

        BigDecimal afterDeposit = account.getCurrentBalance();
        accountServices.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("50.00"));
        assertNotEquals(afterDeposit, account.getCurrentBalance(), "Balance should change after withdrawal");
        assertEquals(originalBalance.add(new BigDecimal("50.00")), account.getCurrentBalance(),
                "Final balance should be original + net change");
    }
}