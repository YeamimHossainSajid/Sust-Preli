package com.hackathon.investigator.dto;

import com.hackathon.investigator.entity.TransactionRecord;

public record TransactionMatchAnalysis(
        TransactionRecord matchedTransaction,
        int matchScore,
        boolean ambiguousMatch,
        int plausibleMatchCount,
        boolean establishedRecipientPattern,
        boolean duplicatePaymentScenario
) {
    public static TransactionMatchAnalysis noMatch() {
        return new TransactionMatchAnalysis(null, 0, false, 0, false, false);
    }

    public static TransactionMatchAnalysis ambiguous(int candidateCount) {
        return new TransactionMatchAnalysis(null, 0, true, candidateCount, false, false);
    }

    public static TransactionMatchAnalysis resolved(
            TransactionRecord transaction,
            int score,
            boolean establishedRecipientPattern,
            boolean duplicatePaymentScenario
    ) {
        return new TransactionMatchAnalysis(
                transaction,
                score,
                false,
                1,
                establishedRecipientPattern,
                duplicatePaymentScenario
        );
    }
}
