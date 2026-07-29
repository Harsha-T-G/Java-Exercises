package org.example.models;

import org.example.constants.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        String TransactionID,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        LocalDateTime timestamp
) {}

