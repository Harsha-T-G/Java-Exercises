package org.example.models;

import org.example.constants.TransactionType;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final String TEST_TRANSACTION_ID = "txn-123-abc";
    private static final TransactionType TEST_TYPE = TransactionType.DEPOSIT;
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("150.75");
    private static final BigDecimal TEST_BALANCE_BEFORE = new BigDecimal("1000.00");
    private static final BigDecimal TEST_BALANCE_AFTER = new BigDecimal("1150.75");
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2023, 6, 15, 14, 30, 0);

    @Test
    void testConstructorAndGetters() {
        Transaction transaction = new Transaction(
                TEST_TRANSACTION_ID,
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );

        assertEquals(TEST_TRANSACTION_ID, transaction.TransactionID());
        assertEquals(TEST_TYPE, transaction.type());
        assertEquals(TEST_AMOUNT, transaction.amount());
        assertEquals(TEST_BALANCE_BEFORE, transaction.balanceBefore());
        assertEquals(TEST_BALANCE_AFTER, transaction.balanceAfter());
        assertEquals(TEST_TIMESTAMP, transaction.timestamp());
    }

    @Test
    void testConstructorWithNullValues() {

        Transaction transaction = new Transaction(
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertNull(transaction.TransactionID());
        assertNull(transaction.type());
        assertNull(transaction.amount());
        assertNull(transaction.balanceBefore());
        assertNull(transaction.balanceAfter());
        assertNull(transaction.timestamp());
    }

    @Test
    void testImmutability() {
        Transaction original = new Transaction(
                TEST_TRANSACTION_ID,
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );

        Transaction copy = new Transaction(
                TEST_TRANSACTION_ID,
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
    }

    @Test
    void testEqualsAndHashCode() {
        Transaction t1 = new Transaction(
                TEST_TRANSACTION_ID,
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );

        Transaction t2 = new Transaction(
                TEST_TRANSACTION_ID,
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );

        Transaction t3 = new Transaction(
                "different-id",
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );

        // Equal objects should be equal and have same hashCode
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        // Different objects should not be equal
        assertNotEquals(t1, t3);
        assertNotEquals(t1.hashCode(), t3.hashCode());

        // Reflexive
        assertEquals(t1, t1);

        // Symmetric
        assertEquals(t1, t2);
        assertEquals(t2, t1);

        // Transitive
        Transaction t4 = new Transaction(
                TEST_TRANSACTION_ID,
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );
        assertEquals(t1, t2);
        assertEquals(t2, t4);
        assertEquals(t1, t4);

        // Consistent
        assertEquals(t1, t2);
        assertEquals(t1, t2);

        // Not equal to null
        assertNotEquals(null, t1);
    }

    @Test
    void testToString() {
        Transaction transaction = new Transaction(
                TEST_TRANSACTION_ID,
                TEST_TYPE,
                TEST_AMOUNT,
                TEST_BALANCE_BEFORE,
                TEST_BALANCE_AFTER,
                TEST_TIMESTAMP
        );

        String toString = transaction.toString();
        assertTrue(toString.contains(TEST_TRANSACTION_ID));
        assertTrue(toString.contains(TEST_TYPE.toString()));
        assertTrue(toString.contains(TEST_AMOUNT.toString()));
        assertTrue(toString.contains(TEST_BALANCE_BEFORE.toString()));
        assertTrue(toString.contains(TEST_BALANCE_AFTER.toString()));
        assertTrue(toString.contains(TEST_TIMESTAMP.toString()));
    }

    @Test
    void testWithRealisticTransactionData() {
        // Test with values that might appear in real banking transactions
        Transaction deposit = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                new BigDecimal("500.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("2000.00"),
                LocalDateTime.now()
        );

        Transaction withdrawal = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.WITHDRAWAL,
                new BigDecimal("200.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("1800.00"),
                LocalDateTime.now()
        );

        assertEquals(TransactionType.DEPOSIT, deposit.type());
        assertEquals(TransactionType.WITHDRAWAL, withdrawal.type());
        assertEquals(new BigDecimal("500.00"), deposit.amount());
        assertEquals(new BigDecimal("200.00"), withdrawal.amount());
        assertEquals(new BigDecimal("1500.00"), deposit.balanceBefore());
        assertEquals(new BigDecimal("2000.00"), deposit.balanceAfter());
        assertEquals(new BigDecimal("2000.00"), withdrawal.balanceBefore());
        assertEquals(new BigDecimal("1800.00"), withdrawal.balanceAfter());
    }
}