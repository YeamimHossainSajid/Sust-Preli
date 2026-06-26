package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.exception.SemanticValidationException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SemanticValidationLayer {

    private static final int MIN_COMPLAINT_LENGTH = 5;
    private static final int MAX_TRANSACTION_HISTORY = 5;

    public void validate(AnalyzeTicketRequest request) {
        String complaint = request.complaint() == null ? "" : request.complaint().trim();
        if (complaint.length() < MIN_COMPLAINT_LENGTH) {
            throw new SemanticValidationException(
                    "complaint must not be empty and must be at least " + MIN_COMPLAINT_LENGTH + " characters"
            );
        }

        if (request.transactionHistory() == null) {
            return;
        }

        if (request.transactionHistory().size() > MAX_TRANSACTION_HISTORY) {
            throw new SemanticValidationException(
                    "transaction_history must contain at most " + MAX_TRANSACTION_HISTORY + " entries"
            );
        }

        Set<String> transactionIds = new HashSet<>();
        for (var transaction : request.transactionHistory()) {
            if (transaction.transactionId() == null || transaction.transactionId().isBlank()) {
                throw new SemanticValidationException("each transaction_history entry must include transaction_id");
            }
            if (transaction.timestamp() == null) {
                throw new SemanticValidationException("each transaction_history entry must include timestamp");
            }
            if (transaction.type() == null) {
                throw new SemanticValidationException("each transaction_history entry must include type");
            }
            if (transaction.amount() == null) {
                throw new SemanticValidationException("each transaction_history entry must include amount");
            }
            if (transaction.counterparty() == null || transaction.counterparty().isBlank()) {
                throw new SemanticValidationException("each transaction_history entry must include counterparty");
            }
            if (transaction.status() == null) {
                throw new SemanticValidationException("each transaction_history entry must include status");
            }
            if (!transactionIds.add(transaction.transactionId())) {
                throw new SemanticValidationException("duplicate transaction_id: " + transaction.transactionId());
            }
        }
    }
}
