package org.example.service;

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

    private AccountService accountService;
    private static final long TEST_ACCOUNT_NUMBER = 123456789L;
    private static final String TEST_ACCOUNT_HOLDER = "John Doe";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
    }

    @Test
    void givenAccountService_whenCreateAccountWithInitialBalance_thenAccountCreatedSuccessfully() throws Exception {
        // Act
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(1, transactions.size());

        Transaction transaction = transactions.get(0);
        assertEquals(TransactionType.DEPOSIT, transaction.type());
        assertEquals(INITIAL_BALANCE, transaction.amount());
        assertEquals(BigDecimal.ZERO, transaction.balanceBefore());
        assertEquals(INITIAL_BALANCE, transaction.balanceAfter());
    }

    @Test
    void givenAccountService_whenCreateAccountWithoutInitialBalance_thenAccountCreatedWithZeroBalance() throws Exception {
        // Act
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER);
        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(BigDecimal.ZERO, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(0, transactions.size());
    }

    @Test
    void givenAccountService_whenCreateAccountWithNegativeInitialBalance_thenThrowsNegativeInitialBalanceException() {
        // Act & Assert
        assertThrows(NegativeInitialBalanceException.class, () -> {
            accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, new BigDecimal("-100.00"));
        });

        assertFalse(accountService.accountExists(TEST_ACCOUNT_NUMBER));
    }

    @Test
    void givenAccountService_whenCreateDuplicateAccountNumber_thenThrowsDuplicateAccountNumberException() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(DuplicateAccountNumberException.class, () -> {
            accountService.createAccount(TEST_ACCOUNT_NUMBER, "Jane Doe", INITIAL_BALANCE);
        });
    }

    @Test
    void givenAccountService_whenDepositValidAmount_thenBalanceIncreasesAndTransactionRecorded() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal depositAmount = new BigDecimal("500.00");

        // Act
        BigDecimal newBalance = accountService.deposit(TEST_ACCOUNT_NUMBER, depositAmount);
        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(INITIAL_BALANCE.add(depositAmount), newBalance);
        assertEquals(INITIAL_BALANCE.add(depositAmount), account.getCurrentBalance());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size());

        Transaction depositTransaction = transactions.get(1);
        assertEquals(TransactionType.DEPOSIT, depositTransaction.type());
        assertEquals(depositAmount, depositTransaction.amount());
        assertEquals(INITIAL_BALANCE, depositTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE.add(depositAmount), depositTransaction.balanceAfter());
    }

    @Test
    void givenAccountService_whenDepositZeroAmount_thenThrowsInvalidAmountExceptionAndBalanceUnchanged() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountService.deposit(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
        });

        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void givenAccountService_whenDepositNegativeAmount_thenThrowsInvalidAmountExceptionAndBalanceUnchanged() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountService.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("-50.00"));
        });

        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void givenBlockedAccount_whenDeposit_thenThrowsAccountBlockedExceptionAndBalanceUnchanged() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountService.blockAccount(TEST_ACCOUNT_NUMBER);

        // Act & Assert
        assertThrows(AccountBlockedException.class, () -> {
            accountService.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        });

        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void givenAccountService_whenWithdrawValidAmount_thenBalanceDecreasesAndTransactionRecorded() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal withdrawalAmount = new BigDecimal("300.00");

        // Act
        BigDecimal newBalance = accountService.withdraw(TEST_ACCOUNT_NUMBER, withdrawalAmount);
        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), newBalance);
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), account.getCurrentBalance());

        List<Transaction> transactions = account.getTransactionHistory();
        assertEquals(2, transactions.size());

        Transaction withdrawalTransaction = transactions.get(1);
        assertEquals(TransactionType.WITHDRAWAL, withdrawalTransaction.type());
        assertEquals(withdrawalAmount, withdrawalTransaction.amount());
        assertEquals(INITIAL_BALANCE, withdrawalTransaction.balanceBefore());
        assertEquals(INITIAL_BALANCE.subtract(withdrawalAmount), withdrawalTransaction.balanceAfter());
    }

    @Test
    void givenAccountService_whenWithdrawalAmountExceedsBalance_thenThrowsInsufficientFundsException() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        BigDecimal withdrawalAmount = INITIAL_BALANCE.add(new BigDecimal("100.00"));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            accountService.withdraw(TEST_ACCOUNT_NUMBER, withdrawalAmount);
        });

        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void givenAccountService_whenWithdrawZeroAmount_thenThrowsInvalidAmountExceptionAndBalanceUnchanged() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountService.withdraw(TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
        });

        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void givenAccountService_whenWithdrawNegativeAmount_thenThrowsInvalidAmountExceptionAndBalanceUnchanged() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> {
            accountService.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("-50.00"));
        });

        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void givenBlockedAccount_whenWithdraw_thenThrowsAccountBlockedExceptionAndBalanceUnchanged() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountService.blockAccount(TEST_ACCOUNT_NUMBER);

        // Act & Assert
        assertThrows(AccountBlockedException.class, () -> {
            accountService.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        });

        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
    }

    @Test
    void givenAccount_whenBlockAndActivateAccount_thenStatusChangesCorrectly() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act & Assert
        assertEquals(AccountStatus.ACTIVE, accountService.getAccountStatus(TEST_ACCOUNT_NUMBER));

        accountService.blockAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.BLOCKED, accountService.getAccountStatus(TEST_ACCOUNT_NUMBER));

        accountService.activateAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.ACTIVE, accountService.getAccountStatus(TEST_ACCOUNT_NUMBER));

        accountService.blockAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.BLOCKED, accountService.getAccountStatus(TEST_ACCOUNT_NUMBER));

        accountService.activateAccount(TEST_ACCOUNT_NUMBER);
        assertEquals(AccountStatus.ACTIVE, accountService.getAccountStatus(TEST_ACCOUNT_NUMBER));
    }

    @Test
    void givenAccountService_whenGetBalance_thenReturnsCorrectBalance() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act
        BigDecimal balance = accountService.getBalance(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(INITIAL_BALANCE, balance);
    }

    @Test
    void givenAccountService_whenGetAccountDetails_thenReturnsAccountWithCorrectFields() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Act
        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);

        // Assert
        assertEquals(TEST_ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(TEST_ACCOUNT_HOLDER, account.getAccountHolderName());
        assertEquals(INITIAL_BALANCE, account.getCurrentBalance());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    @Test
    void givenAccountService_whenCheckAccountExists_thenReturnsCorrectBoolean() throws Exception {
        // Assert
        assertFalse(accountService.accountExists(TEST_ACCOUNT_NUMBER));

        // Act
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);

        // Assert
        assertTrue(accountService.accountExists(TEST_ACCOUNT_NUMBER));
        assertFalse(accountService.accountExists(999999999L));
    }

    @Test
    void givenAccountWithTransactions_whenGetTransactionHistory_thenReturnsUnmodifiableList() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        accountService.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("50.00"));

        // Act
        List<Transaction> history = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER).getTransactionHistory();
        int originalSize = history.size();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, history::clear);
        assertEquals(originalSize, accountService.getAccountDetails(TEST_ACCOUNT_NUMBER).getTransactionHistory().size());

        // Assert
        assertFalse(accountService.getAccountDetails(TEST_ACCOUNT_NUMBER).getTransactionHistory().isEmpty());
    }

    @Test
    void givenAccount_whenBalanceChangedOnlyThroughDepositWithdrawal_thenBalanceReflectsNetChange() throws Exception {
        // Arrange
        accountService.createAccount(TEST_ACCOUNT_NUMBER, TEST_ACCOUNT_HOLDER, INITIAL_BALANCE);
        Account account = accountService.getAccountDetails(TEST_ACCOUNT_NUMBER);
        BigDecimal originalBalance = account.getCurrentBalance();

        // Act
        accountService.deposit(TEST_ACCOUNT_NUMBER, new BigDecimal("100.00"));
        assertNotEquals(originalBalance, account.getCurrentBalance());

        BigDecimal afterDeposit = account.getCurrentBalance();
        accountService.withdraw(TEST_ACCOUNT_NUMBER, new BigDecimal("50.00"));
        assertNotEquals(afterDeposit, account.getCurrentBalance());
        assertEquals(originalBalance.add(new BigDecimal("50.00")), account.getCurrentBalance());
    }
}