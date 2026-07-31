package org.example.models;

import org.example.constants.AccountStatus;
import org.example.constants.TransactionType;
import org.example.exception.NegativeInitialBalanceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Account class.
 * Tests cover construction, field modifications, and transaction history immutability.
 * Business logic tests (deposit, withdraw, block, activate) are in AccountServicesTest.
 */
class AccountTest {

    private Account account;
    private static final long TEST_ACCOUNT_NUMBER = 987654321L;
    private static final String TEST_ACCOUNT_HOLDER = "ABCD";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        // Initialize a fresh account before each test
        account = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE, AccountStatus.ACTIVE);
    }

    /**
     * Tests that an account is correctly initialized with the provided initial balance
     * and that an initial deposit transaction is recorded.
     */
    @Test
    void testConstructorWithInitialBalance() {
        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        // Verify that an initial deposit transaction was created
        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(1, transactions.size(), "Expected exactly one transaction (initial deposit)");

        Transaction initialTransaction = transactions.get(0);
        assertEquals(TransactionType.DEPOSIT, initialTransaction.type());
        assertEquals(INITIAL_BALANCE, initialTransaction.amount());
        assertEquals(BigDecimal.ZERO, initialTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE, initialTransaction.balanceAfter());
    }

    /**
     * Tests that an account can be created with zero initial balance
     * and that no transaction is recorded when the initial balance is zero.
     */
    @Test
    void testConstructorWithoutInitialBalance() {
        // Act
        Account zeroBalanceAccount = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, zeroBalanceAccount.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, zeroBalanceAccount.getAccountHolderName());
        assertEquals(BigDecimal.ZERO, zeroBalanceAccount.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, zeroBalanceAccount.getAccountStatus());

        // No transactions should be present for zero initial balance
        List<Transaction> transactions = zeroBalanceAccount.getTransactionHistory();
        assertEquals(0, transactions.size(), "Expected zero transactions for zero initial balance");
    }

    /**
     * Ensures that creating an account with a negative initial balance
     * throws the appropriate exception.
     */
    @Test
    void testConstructorWithNegativeInitialBalance() {
        // Act & Assert
        assertThrows(NegativeInitialBalanceException.class, () -> {
            new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, new BigDecimal("-100.00"), AccountStatus.ACTIVE);
        });
    }

    /**
     * Tests that the account holder's name can be updated via the setter.
     */
    @Test
    void testSetAccountHolderName() {
        // Arrange
        String newName = "EFGH";

        // Act
        account.setAccountHolderName(newName);

        // Assert
        assertEquals(newName, account.getAccountHolderName(), "Account holder name should be updated");
    }

    /**
     * Tests that the current balance can be updated via the setter.
     */
    @Test
    void testSetCurrentBalance() {
        // Arrange
        BigDecimal newBalance = new BigDecimal("750.00");

        // Act
        account.setCurrentBalance(newBalance);

        // Assert
        assertEquals(newBalance, account.getCurrentBalance());
    }

    /**
     * Tests that the account status can be updated via the setter.
     */
    @Test
    void testSetAccountStatus() {
        // Act
        account.setAccountStatus(AccountStatus.BLOCKED);

        // Assert
        assertEquals(AccountStatus.BLOCKED, account.getAccountStatus());

        // Act: Change back to active
        account.setAccountStatus(AccountStatus.ACTIVE);

        // Assert
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    /**
     * Tests that the transaction history returned by getTransactionHistory is immutable.
     * Attempting to modify the returned list should throw an UnsupportedOperationException.
     */
    @Test
    void testTransactionHistoryIsUnmodifiable() {
        // Get initial transaction count (should be 1 for initial deposit)
        List<Transaction> history = account.getTransactionHistory();
        int initialSize = history.size();
        assertEquals(1, initialSize, "Account should start with one transaction (initial deposit)");

        // Attempt to modify the list should fail
        assertThrows(UnsupportedOperationException.class, () -> {
            history.clear();
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            history.add(new Transaction(
                    "test", TransactionType.WITHDRAWAL, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, java.time.LocalDateTime.now()));
        });

        // Assert underlying data unchanged
        assertEquals(initialSize, account.getTransactionHistory().size());
        assertFalse(account.getTransactionHistory().isEmpty());
    }

    /**
     * Tests that we can manually add transactions to the history.
     * Note: Account starts with 1 transaction (initial deposit) when created with balance.
     */
    @Test
    void testAddTransaction() {
        // Account starts with 1 transaction (initial deposit from constructor)
        int initialCount = account.getTransactionHistory().size();
        assertEquals(1, initialCount);

        // Add two transactions
        Transaction depositTx = new Transaction(
                "deposit-1", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                BigDecimal.ZERO, new BigDecimal("100.00"), java.time.LocalDateTime.now());
        Transaction withdrawalTx = new Transaction(
                "withdrawal-1", TransactionType.WITHDRAWAL, new BigDecimal("50.00"),
                new BigDecimal("100.00"), new BigDecimal("50.00"), java.time.LocalDateTime.now());

        account.addTransaction(depositTx);
        account.addTransaction(withdrawalTx);

        // Should now have 3 transactions total
        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(3, transactions.size());

        // Verify the transactions we added are at the end
        assertEquals(TransactionType.DEPOSIT, transactions.get(1).type());
        assertEquals(new BigDecimal("100.00"), transactions.get(1).amount());
        assertEquals(TransactionType.WITHDRAWAL, transactions.get(2).type());
        assertEquals(new BigDecimal("50.00"), transactions.get(2).amount());
    }

    /**
     * Tests adding transactions to an account with zero initial balance.
     */
    @Test
    void testAddTransactionZeroBalanceAccount() {
        // Create account with zero balance (should have 0 transactions initially)
        Account zeroBalanceAccount = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);
        assertEquals(0, zeroBalanceAccount.getTransactionHistory().size());

        // Add a transaction
        Transaction depositTx = new Transaction(
                "deposit-1", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                BigDecimal.ZERO, new BigDecimal("100.00"), java.time.LocalDateTime.now());
        zeroBalanceAccount.addTransaction(depositTx);

        // Should now have 1 transaction
        List<Transaction> transactions = zeroBalanceAccount.getTransactionHistory();
        assertEquals(1, transactions.size());
        assertEquals(TransactionType.DEPOSIT, transactions.get(0).type());
        assertEquals(new BigDecimal("100.00"), transactions.get(0).amount());
    }
}