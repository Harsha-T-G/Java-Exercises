package org.example.models;

import org.example.constants.AccountStatus;
import org.example.constants.TransactionType;
import org.example.exception.NegativeInitialBalanceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;
    private static final long TEST_ACCOUNT_NUMBER = 987654321L;
    private static final String TEST_ACCOUNT_HOLDER = "ABCD";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        account = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE, AccountStatus.ACTIVE);
    }

    @Test
    void givenAccountCreatedWithInitialBalance_whenInitialized_thenAccountFieldsSetCorrectly() {
        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(1, transactions.size());
        Transaction initialTransaction = transactions.get(0);
        assertEquals(TransactionType.DEPOSIT, initialTransaction.type());
        assertEquals(INITIAL_BALANCE, initialTransaction.amount());
        assertEquals(BigDecimal.ZERO, initialTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE, initialTransaction.balanceAfter());
    }

    @Test
    void givenAccountCreatedWithoutInitialBalance_whenInitialized_thenZeroBalanceAndNoTransactions() {
        // Act
        Account zeroBalanceAccount = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, zeroBalanceAccount.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, zeroBalanceAccount.getAccountHolderName());
        assertEquals(BigDecimal.ZERO, zeroBalanceAccount.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, zeroBalanceAccount.getAccountStatus());

        List<Transaction> transactions = zeroBalanceAccount.getTransactionHistory();
        assertEquals(0, transactions.size());
    }

    @Test
    void givenAccountCreatedWithNegativeInitialBalance_whenInitialized_thenThrowsNegativeInitialBalanceException() {
        // Act & Assert
        assertThrows(NegativeInitialBalanceException.class, () -> {
            new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, new BigDecimal("-100.00"), AccountStatus.ACTIVE);
        });
    }

    @Test
    void givenAccount_whenSetAccountHolderName_thenNameIsUpdated() {
        // Arrange
        String newName = "EFGH";

        // Act
        account.setAccountHolderName(newName);

        // Assert
        assertEquals(newName, account.getAccountHolderName());
    }

    @Test
    void givenAccount_whenSetCurrentBalance_thenBalanceIsUpdated() {
        // Arrange
        BigDecimal newBalance = new BigDecimal("750.00");

        // Act
        account.setCurrentBalance(newBalance);

        // Assert
        assertEquals(newBalance, account.getCurrentBalance());
    }

    @Test
    void givenAccount_whenSetAccountStatus_thenStatusChangesAccordingly() {
        // Act: set to blocked
        account.setAccountStatus(AccountStatus.BLOCKED);
        // Assert
        assertEquals(AccountStatus.BLOCKED, account.getAccountStatus());

        // Act: set back to active
        account.setAccountStatus(AccountStatus.ACTIVE);
        // Assert
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    @Test
    void givenAccountWithTransactionHistory_whenAttemptingToModify_thenThrowsUnsupportedOperationException() {
        // Arrange
        List<Transaction> history = account.getTransactionHistory();
        int initialSize = history.size();
        assertEquals(1, initialSize);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            history.clear();
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            history.add(new Transaction(
                    "test", TransactionType.WITHDRAWAL, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, java.time.LocalDateTime.now()));
        });

        // Assert
        assertEquals(initialSize, account.getTransactionHistory().size());
        assertFalse(account.getTransactionHistory().isEmpty());
    }

    @Test
    void givenAccountWithInitialTransaction_whenAddingTwoTransactions_thenTransactionHistoryContainsThreeTransactions() {
        // Arrange
        int initialCount = account.getTransactionHistory().size();
        assertEquals(1, initialCount);

        // Act
        Transaction depositTx = new Transaction(
                "deposit-1", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                BigDecimal.ZERO, new BigDecimal("100.00"), java.time.LocalDateTime.now());
        Transaction withdrawalTx = new Transaction(
                "withdrawal-1", TransactionType.WITHDRAWAL, new BigDecimal("50.00"),
                new BigDecimal("100.00"), new BigDecimal("50.00"), java.time.LocalDateTime.now());
        account.addTransaction(depositTx);
        account.addTransaction(withdrawalTx);

        // Assert
        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(3, transactions.size());
        assertEquals(TransactionType.DEPOSIT, transactions.get(1).type());
        assertEquals(new BigDecimal("100.00"), transactions.get(1).amount());
        assertEquals(TransactionType.WITHDRAWAL, transactions.get(2).type());
        assertEquals(new BigDecimal("50.00"), transactions.get(2).amount());
    }

    @Test
    void givenAccountWithZeroBalance_whenAddingTransaction_thenTransactionHistoryContainsOneTransaction() {
        // Arrange
        Account zeroBalanceAccount = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);
        assertEquals(0, zeroBalanceAccount.getTransactionHistory().size());

        // Act
        Transaction depositTx = new Transaction(
                "deposit-1", TransactionType.DEPOSIT, new BigDecimal("100.00"),
                BigDecimal.ZERO, new BigDecimal("100.00"), java.time.LocalDateTime.now());
        zeroBalanceAccount.addTransaction(depositTx);

        // Assert
        List<Transaction> transactions = zeroBalanceAccount.getTransactionHistory();
        assertEquals(1, transactions.size());
        assertEquals(TransactionType.DEPOSIT, transactions.get(0).type());
        assertEquals(new BigDecimal("100.00"), transactions.get(0).amount());
    }
}