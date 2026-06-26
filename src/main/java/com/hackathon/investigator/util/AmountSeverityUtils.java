package com.hackathon.investigator.util;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.Severity;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public final class AmountSeverityUtils {

    private static final BigDecimal LOW_MAX = new BigDecimal("500");
    private static final BigDecimal HIGH_MIN = new BigDecimal("5000");
    private static final BigDecimal CRITICAL_MIN = new BigDecimal("25000");

    private AmountSeverityUtils() {
    }

    public static BigDecimal resolveInvestigationAmount(
            TransactionRecord matchedTransaction,
            AiExtractionResult extraction,
            String complaint,
            List<TransactionRecord> transactions
    ) {
        if (matchedTransaction != null && matchedTransaction.getAmount() != null) {
            return matchedTransaction.getAmount();
        }
        if (extraction.amount() != null) {
            return extraction.amount();
        }
        BigDecimal fromComplaint = TextUtils.extractAmount(complaint);
        if (fromComplaint != null) {
            return fromComplaint;
        }
        if (transactions == null || transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return transactions.stream()
                .map(TransactionRecord::getAmount)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    public static Severity severityFromAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Severity.LOW;
        }
        if (amount.compareTo(CRITICAL_MIN) >= 0) {
            return Severity.CRITICAL;
        }
        if (amount.compareTo(HIGH_MIN) >= 0) {
            return Severity.HIGH;
        }
        if (amount.compareTo(LOW_MAX) >= 0) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }

    public static Severity bumpSeverity(Severity severity) {
        return switch (severity) {
            case LOW -> Severity.MEDIUM;
            case MEDIUM -> Severity.HIGH;
            case HIGH, CRITICAL -> Severity.CRITICAL;
        };
    }

    public static boolean amountWithinTolerance(BigDecimal actual, BigDecimal expected) {
        if (actual == null || expected == null || expected.compareTo(BigDecimal.ZERO) <= 0) {
            return actual != null && expected != null && actual.compareTo(expected) == 0;
        }
        BigDecimal difference = actual.subtract(expected).abs();
        BigDecimal tolerance = expected.multiply(new BigDecimal("0.10"));
        return difference.compareTo(tolerance) <= 0;
    }
}
