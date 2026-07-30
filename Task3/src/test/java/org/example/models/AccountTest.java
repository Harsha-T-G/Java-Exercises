package org.example.models;

import org.example.constants.AccountStatus;
import org.example.constants.TransactionType;
import org.example.exception.AccountBlockedException;
import org.example.exception.InsufficientFundsException;
import org.example.exception.InvalidAmountException;
import org.example.exception.NegativeInitialBalanceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;
    private static final long TEST_ACCOUNT_NUMBER = 987654321L;
    private static final String TEST_ACCOUNT_HOLDER = "Jane Smith";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        account = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE, AccountStatus.ACTIVE);
    }

    @Test
    void testConstructorWithInitialBalance() {
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        // Should have initial deposit transaction
        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(1, transactions.size());

        Transaction initialTransaction = transactions.get(0);
        assertEquals(TransactionType.DEPOSIT, initialTransaction.type());
        assertEquals(INITIAL_BALANCE, initialTransaction.amount());
        assertEquals(BigDecimal.ZERO, initialTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE, initialTransaction.balanceAfter());
    }

    @Test
    void testConstructorWithoutInitialBalance() {
        Account zeroBalanceAccount = new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);

        assertEquals(TEST_ACCOUNT_NUMBER, zeroBalanceAccount.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, zeroBalanceAccount.getAccountHolderName());
        assertEquals(BigDecimal.ZERO, zeroBalanceAccount.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, zeroBalanceAccount.getAccountStatus());

        // Should have no transactions
        List<Transaction> transactions = zeroBalanceAccount.getTransactionHistory();
        assertEquals(0, transactions.size());
    }

    @Test
    void testConstructorWithNegativeInitialBalance() {
        assertThrows(NegativeInitialBalanceException.class, () -> {
            new Account(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, new BigDecimal("-100.00"), AccountStatus.ACTIVE);
        });
    }

    @Test
    void testSetAccountHolderName() {
        String newName = "Johnny Smith";
        account.setAccountHolderName(newName);
        assertEquals(newName, account.getAccountHolderName());
    }

    @Test
    void testSuccessfulDeposit() throws Exception {
        BigDecimal depositAmount = new BigDecimal("2500");
        BigDecimal newBalance = account.deposit(depositAmount);
        assertEquals(INITIAL_BALANCE.add(depositAmount),newBalance,"The New Balance differ from the required amount");

        assertEquals(INITIAL_BALANCE.add(depositAmount), account.getCurrentBalance());
        assertEquals(newBalance,account.getCurrentBalance());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size()); // Initial + deposit

        Transaction depositTx = transactions.get(1);
        assertEquals(TransactionType.DEPOSIT, depositTx.type());
        assertEquals(depositAmount, depositTx.amount());
        assertEquals(INITIAL_BALANCE, depositTx.balanceBefore());
        assertEquals(INITIAL_BALANCE.add(depositAmount), depositTx.balanceAfter());
    }

    @Test
    void testDepositWithZeroAmount() {
        assertThrows(InvalidAmountException.class, () -> {
            account.deposit(BigDecimal.ZERO);
        });

        // Balance should remain unchanged
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void testDepositWithNegativeAmount() {
        assertThrows(InvalidAmountException.class, () -> {
            account.deposit(new BigDecimal("-50.00"));
        });

        // Balance should remain unchanged
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void testDepositOnBlockedAccount() throws Exception {
        // First block the account
        account.blockAccount();
        assertEquals(AccountStatus.BLOCKED, account.getAccountStatus());

        // Then try to deposit
        assertThrows(AccountBlockedException.class, () -> {
            account.deposit(new BigDecimal("100.00"));
        });

        // Balance should remain unchanged
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void testSuccessfulWithdrawal() throws Exception {
        BigDecimal withdrawalAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = INITIAL_BALANCE.subtract(withdrawalAmount);

        BigDecimal actualBalance = account.withdraw(withdrawalAmount);

        assertEquals(expectedBalance, actualBalance);
        assertEquals(expectedBalance, account.getCurrentBalance());

        // Verify transaction recorded
        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size()); // Initial + withdrawal

        Transaction withdrawalTx = transactions.get(1);
        assertEquals(TransactionType.WITHDRAWAL, withdrawalTx.type());
        assertEquals(withdrawalAmount, withdrawalTx.amount());
        assertEquals(INITIAL_BALANCE, withdrawalTx.balanceBefore());
        assertEquals(expectedBalance, withdrawalTx.balanceAfter());
    }

    @Test
    void testWithdrawalWithInsufficientFunds() {
        BigDecimal excessiveAmount = INITIAL_BALANCE.add(new BigDecimal("100.00"));

        assertThrows(InsufficientFundsException.class, () -> {
            account.withdraw(excessiveAmount);
        });

        // Balance should remain unchanged
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void testWithdrawalWithZeroAmount() {
        assertThrows(InvalidAmountException.class, () -> {
            account.withdraw(BigDecimal.ZERO);
        });

        // Balance should remain unchanged
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void testWithdrawalWithNegativeAmount() {
        assertThrows(InvalidAmountException.class, () -> {
            account.withdraw(new BigDecimal("-50.00"));
        });

        // Balance should remain unchanged
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void testWithdrawalOnBlockedAccount() throws Exception {
        // First block the account
        account.blockAccount();
        assertEquals(AccountStatus.BLOCKED, account.getAccountStatus());

        // Then try to withdraw
        assertThrows(AccountBlockedException.class, () -> {
            account.withdraw(new BigDecimal("100.00"));
        });

        // Balance should remain unchanged
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void testBlockAndUnblockAccount() {
        // Initially active
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        // Block account
        account.blockAccount();
        assertEquals(AccountStatus.BLOCKED, account.getAccountStatus());

        // Unblock account
        account.activateAccount();
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        // Block again (idempotent)
        account.blockAccount();
        assertEquals(AccountStatus.BLOCKED, account.getAccountStatus());

        // Unblock again (idempotent)
        account.activateAccount();
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    @Test
    void testTransactionHistoryIsUnmodifiable() throws Exception {
        // Make a transaction to have something in history
        account.deposit(new BigDecimal("100.00"));

        List<Transaction> history = account.getTransactionHistory();
        int originalSize = history.size();

        // Attempt to modify the returned list should fail
        assertThrows(UnsupportedOperationException.class, () -> {
            history.clear();
        });

        // Original list should remain unchanged
        assertEquals(originalSize, account.getTransactionHistory().size());
        assertFalse(account.getTransactionHistory().isEmpty());
    }

    @Test
    void testAccountNumberIsImmutable() {
        // accountNumber is final - we can't directly test setting it,
        // but we can verify there's no setter method
        // The fact that we can't call account.setAccountNumber(...) proves immutability
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        // This would not compile if we tried: account.setAccountNumber(123L);
    }

    @Test
    void testBalanceHasNoPublicSetter() {
        // There is no setBalance method in the Account class
        // We can only change balance through deposit/withdraw
        BigDecimal originalBalance = account.getCurrentBalance();

        // Change balance through valid method
        account.deposit(new BigDecimal("100.00"));
        assertNotEquals(originalBalance, account.getCurrentBalance());

        // Change it back
        account.withdraw(new BigDecimal("100.00"));
        assertEquals(originalBalance, account.getCurrentBalance());
    }
}