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

class AccountServicesTest {

    private AccountServices accountServices;
    private static final long TEST_ACCOUNT_NUMBER = 123456789L;
    private static final String TEST_ACCOUNT_HOLDER = "John Doe";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @BeforeEach
    void setUp() {
        accountServices = new AccountServices();
    }

    @Test
    void testCreateAccountWithInitialBalance() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);


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

    @Test
    void testCreateAccountWithoutInitialBalance() throws Exception {

        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);


        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(BigDecimal.ZERO, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());


        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(0, transactions.size(), "Should have no transactions when initial balance is zero");
    }

    @Test
    void testCreateAccountWithNegativeInitialBalance() {
        assertThrows(NegativeInitialBalanceException.class, () -> {
            accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, new BigDecimal("-100.00"));
        });

        assertFalse(accountServices.accountExists(TEST_ACCOUNT_NUMBER),
                   "Account should not exist after failed creation");
    }

    @Test
    void testCreateDuplicateAccountNumber() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        assertThrows(DuplicateAccountNumberException.class, () -> {
            accountServices.createAccount(TEST_ACCOUNT_NUMBER, "Jane Doe", INITIAL_BALANCE);
        });
    }

    @Test
    void testSuccessfulDeposit() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal depositAmount = new BigDecimal("500.00");

        BigDecimal newBalance = accountServices.deposit(TEST_ACCOUNT_NUMBER, depositAmount);

        assertEquals(INITIAL_BALANCE.add(depositAmount), newBalance);

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE.add(depositAmount), account.getCurrentBalance());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size(), "Should have initial deposit + new deposit");

        Transaction depositTransaction = transactions.get(1); // Most recent transaction
        assertEquals(TransactionType.DEPOSIT, depositTransaction.type());
        assertEquals(depositAmount, depositTransaction.amount());
        assertEquals(INITIAL_BALANCE, depositTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE.add(depositAmount), depositTransaction.balanceAfter());
    }

    @Test
    void testDepositWithZeroAmount() throws Exception {
        // Arrange: Create account
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        assertThrows(InvalidAmountException.class, () -> {
            accountServices.deposit(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed deposit");
    }

    @Test
    void testDepositWithNegativeAmount() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        assertThrows(InvalidAmountException.class, () -> {
            accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("-50.00"));
        });
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed deposit");
    }

    @Test
    void testDepositOnBlockedAccount() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountServices.blockAccount(TEST_ACCOUNT_NUMBER);

        assertThrows(AccountBlockedException.class, () -> {
            accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged when account is blocked");
    }

    @Test
    void testSuccessfulWithdrawal() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal withdrawalAmount = new BigDecimal("300.00");

        BigDecimal newBalance = accountServices.withdraw(TEST_ACCOUNT_NUMBER, withdrawalAmount);

        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), newBalance);

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), account.getCurrentBalance());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size(), "Should have initial deposit + withdrawal");

        Transaction withdrawalTransaction = transactions.get(1); // Most recent transaction
        assertEquals(TransactionType.WITHDRAWAL, withdrawalTransaction.type());
        assertEquals(withdrawalAmount, withdrawalTransaction.amount());
        assertEquals(INITIAL_BALANCE, withdrawalTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), withdrawalTransaction.balanceAfter());
    }

    @Test
    void testWithdrawalWithInsufficientFunds() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal withdrawalAmount = INITIAL_BALANCE.add(new BigDecimal("100.00")); // More than balance

        assertThrows(InsufficientFundsException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, withdrawalAmount);
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed withdrawal");
    }

    @Test
    void testWithdrawalWithZeroAmount() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        assertThrows(InvalidAmountException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed withdrawal");
    }

    @Test
    void testWithdrawalWithNegativeAmount() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        assertThrows(InvalidAmountException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("-50.00"));
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged after failed withdrawal");
    }

    @Test
    void testWithdrawalOnBlockedAccount() throws Exception {

        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountServices.blockAccount(TEST_ACCOUNT_NUMBER);

        assertThrows(AccountBlockedException.class, () -> {
            accountServices.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        });

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance(), "Balance should remain unchanged when account is blocked");
    }

    @Test
    void testBlockAndActivateAccount() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

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

    @Test
    void testGetBalance() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        BigDecimal balance = accountServices.getBalance(TEST_ACCOUNT_NUMBER);

        assertEquals(INITIAL_BALANCE, balance);
    }

    @Test
    void testGetAccountDetails() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);

        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    @Test
    void testAccountExists() throws Exception {
        assertFalse(accountServices.accountExists(TEST_ACCOUNT_NUMBER));

        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        assertTrue(accountServices.accountExists(TEST_ACCOUNT_NUMBER));

        assertFalse(accountServices.accountExists(999999999L));
    }

    @Test
    void testTransactionHistoryIsUnmodifiable() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("50.00"));

        List<Transaction> history = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER).getTransactionHistory();
        int originalSize = history.size();

        assertThrows(UnsupportedOperationException.class, history::clear);
        assertEquals(originalSize, accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER).getTransactionHistory().size(),
                    "Original transaction history should remain unmodified");
    }

    @Test
    void testBalanceCanOnlyBeChangedThroughDepositWithdrawal() throws Exception {
        accountServices.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        Account account = accountServices.getAccountDetails(TEST_ACCOUNT_NUMBER);


        BigDecimal originalBalance = account.getCurrentBalance();

        accountServices.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        assertNotEquals(originalBalance, account.getCurrentBalance(), "Balance should change after deposit");

        BigDecimal afterDeposit = account.getCurrentBalance();
        accountServices.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("50.00"));
        assertNotEquals(afterDeposit, account.getCurrentBalance(), "Balance should change after withdrawal");
        assertEquals(originalBalance.add(new BigDecimal("50.00")), account.getCurrentBalance(),
                    "Final balance should be original + net change");
    }
}