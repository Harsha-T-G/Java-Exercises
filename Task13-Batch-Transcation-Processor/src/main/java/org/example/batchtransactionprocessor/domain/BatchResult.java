package org.example.batchtransactionprocessor.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record BatchResult(
        List<String> successfulTransactionIds,
        List<TransactionFailure> failures,
        Map<String, BigDecimal> finalAccountBalances) {

    public BatchResult {
        successfulTransactionIds = List.copyOf(successfulTransactionIds);
        failures = List.copyOf(failures);
        finalAccountBalances = Collections.unmodifiableMap(
                new LinkedHashMap<>(finalAccountBalances));
    }
}
