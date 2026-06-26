package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.dto.TransactionMatchAnalysis;
import com.hackathon.investigator.dto.TransactionMatchResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.enums.TransactionType;
import com.hackathon.investigator.service.TransactionMatcherService;
import com.hackathon.investigator.util.ComplaintAnalyzer;
import com.hackathon.investigator.util.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TransactionMatcherServiceImpl implements TransactionMatcherService {

    private static final int AMOUNT_MATCH_SCORE = 5;
    private static final int TYPE_MATCH_SCORE = 4;
    private static final int TIME_MATCH_SCORE = 3;
    private static final int COUNTERPARTY_MATCH_SCORE = 2;
    private static final int STATUS_MATCH_SCORE = 2;

    @Override
    public TransactionMatchAnalysis analyzeMatches(
            List<TransactionRecord> transactions,
            AiExtractionResult extraction,
            String complaint
    ) {
        if (transactions == null || transactions.isEmpty()) {
            return TransactionMatchAnalysis.noMatch();
        }

        Optional<TransactionRecord> duplicate = findDuplicatePaymentTransaction(transactions, complaint);
        if (duplicate.isPresent()) {
            TransactionRecord tx = duplicate.get();
            return TransactionMatchAnalysis.resolved(tx, scoreTransaction(tx, extraction, complaint, transactions), false, true);
        }

        if (shouldTreatAsAmbiguous(transactions, extraction, complaint)) {
            BigDecimal amount = resolveAmount(extraction, complaint);
            int candidateCount = countPlausibleMatches(transactions, amount);
            log.info("Ambiguous transaction match: {} plausible candidates", candidateCount);
            return TransactionMatchAnalysis.ambiguous(Math.max(candidateCount, 2));
        }

        String complaintPhone = TextUtils.extractPhoneNumber(complaint);
        BigDecimal extractedAmount = extraction.amount() != null
                ? extraction.amount()
                : TextUtils.extractAmount(complaint);

        List<TransactionMatchResult> scored = transactions.stream()
                .map(transaction -> new TransactionMatchResult(
                        transaction,
                        scoreTransaction(transaction, extraction, complaint, transactions)
                ))
                .filter(result -> result.score() > 0)
                .sorted(Comparator
                        .comparingInt(TransactionMatchResult::score).reversed()
                        .thenComparing(result -> result.transaction().getTimestamp(), Comparator.reverseOrder()))
                .toList();

        if (scored.isEmpty()) {
            return TransactionMatchAnalysis.noMatch();
        }

        int topScore = scored.get(0).score();
        List<TransactionMatchResult> topMatches = scored.stream()
                .filter(result -> result.score() == topScore)
                .toList();

        if (topMatches.size() > 1) {
            return TransactionMatchAnalysis.ambiguous(topMatches.size());
        }

        TransactionRecord best = topMatches.get(0).transaction();
        boolean establishedRecipient = hasEstablishedRecipientPattern(best, transactions, complaint);
        return TransactionMatchAnalysis.resolved(best, topScore, establishedRecipient, false);
    }

    private Optional<TransactionRecord> findDuplicatePaymentTransaction(
            List<TransactionRecord> transactions,
            String complaint
    ) {
        if (!ComplaintAnalyzer.mentionsDuplicatePayment(complaint)) {
            return Optional.empty();
        }

        List<TransactionRecord> sorted = transactions.stream()
                .sorted(Comparator.comparing(TransactionRecord::getTimestamp))
                .toList();

        TransactionRecord duplicateCandidate = null;
        for (int i = 0; i < sorted.size(); i++) {
            TransactionRecord current = sorted.get(i);
            for (int j = i + 1; j < sorted.size(); j++) {
                TransactionRecord next = sorted.get(j);
                if (isDuplicatePair(current, next)) {
                    duplicateCandidate = next;
                }
            }
        }
        return Optional.ofNullable(duplicateCandidate);
    }

    private boolean isDuplicatePair(TransactionRecord first, TransactionRecord second) {
        if (first.getType() != second.getType()) {
            return false;
        }
        if (first.getAmount().compareTo(second.getAmount()) != 0) {
            return false;
        }
        if (!first.getCounterparty().equals(second.getCounterparty())) {
            return false;
        }
        if (first.getStatus() != TransactionStatus.COMPLETED || second.getStatus() != TransactionStatus.COMPLETED) {
            return false;
        }
        long secondsApart = Math.abs(Duration.between(first.getTimestamp(), second.getTimestamp()).getSeconds());
        return secondsApart <= 1800;
    }

    private boolean shouldTreatAsAmbiguous(
            List<TransactionRecord> transactions,
            AiExtractionResult extraction,
            String complaint
    ) {
        if (TextUtils.extractPhoneNumber(complaint) != null) {
            return false;
        }

        BigDecimal amount = resolveAmount(extraction, complaint);
        if (amount == null) {
            return false;
        }

        if (!ComplaintAnalyzer.mentionsNotReceived(complaint) && !ComplaintAnalyzer.mentionsWrongTransfer(complaint)) {
            return false;
        }

        List<TransactionRecord> amountMatches = transactions.stream()
                .filter(tx -> tx.getAmount().compareTo(amount) == 0)
                .filter(tx -> tx.getType() == TransactionType.TRANSFER)
                .toList();

        long distinctCounterparties = amountMatches.stream()
                .map(TransactionRecord::getCounterparty)
                .distinct()
                .count();

        return amountMatches.size() >= 2 && distinctCounterparties >= 2;
    }

    private int countPlausibleMatches(List<TransactionRecord> transactions, BigDecimal amount) {
        if (amount == null) {
            return transactions.size();
        }
        return (int) transactions.stream()
                .filter(tx -> tx.getAmount().compareTo(amount) == 0)
                .count();
    }

    private BigDecimal resolveAmount(AiExtractionResult extraction, String complaint) {
        return extraction.amount() != null ? extraction.amount() : TextUtils.extractAmount(complaint);
    }

    private boolean hasEstablishedRecipientPattern(
            TransactionRecord matched,
            List<TransactionRecord> transactions,
            String complaint
    ) {
        if (!ComplaintAnalyzer.mentionsWrongTransfer(complaint)) {
            return false;
        }
        if (matched.getType() != TransactionType.TRANSFER) {
            return false;
        }

        long sameCounterpartyTransfers = transactions.stream()
                .filter(tx -> tx.getType() == TransactionType.TRANSFER)
                .filter(tx -> tx.getCounterparty().equals(matched.getCounterparty()))
                .count();

        return sameCounterpartyTransfers >= 2;
    }

    private int scoreTransaction(
            TransactionRecord transaction,
            AiExtractionResult extraction,
            String complaint,
            List<TransactionRecord> transactions
    ) {
        return score(
                transaction,
                extraction,
                extraction.amount() != null ? extraction.amount() : TextUtils.extractAmount(complaint),
                TextUtils.extractPhoneNumber(complaint),
                referenceInstant(transactions),
                extraction.intent()
        );
    }

    private int score(
            TransactionRecord transaction,
            AiExtractionResult extraction,
            BigDecimal extractedAmount,
            String complaintPhone,
            Instant referenceInstant,
            String intent
    ) {
        int score = 0;

        if (extractedAmount != null && transaction.getAmount().compareTo(extractedAmount) == 0) {
            score += AMOUNT_MATCH_SCORE;
        }

        if (extraction.transactionType() != null && extraction.transactionType() == transaction.getType()) {
            score += TYPE_MATCH_SCORE;
        }

        if (isRecentRelativeToBatch(transaction.getTimestamp(), referenceInstant)) {
            score += TIME_MATCH_SCORE;
        }

        if (complaintPhone != null
                && normalizePhone(complaintPhone).equals(normalizePhone(transaction.getCounterparty()))) {
            score += COUNTERPARTY_MATCH_SCORE;
        }

        if (matchesExpectedStatus(intent, transaction.getStatus())) {
            score += STATUS_MATCH_SCORE;
        }

        return score;
    }

    private Instant referenceInstant(List<TransactionRecord> transactions) {
        return transactions.stream()
                .map(TransactionRecord::getTimestamp)
                .max(Instant::compareTo)
                .orElse(Instant.now());
    }

    private boolean isRecentRelativeToBatch(Instant timestamp, Instant referenceInstant) {
        return Duration.between(timestamp, referenceInstant).abs().toHours() <= 72;
    }

    private boolean matchesExpectedStatus(String intent, TransactionStatus status) {
        if (intent == null) {
            return status == TransactionStatus.COMPLETED;
        }
        return switch (intent) {
            case "payment_failed" -> status == TransactionStatus.FAILED;
            case "wrong_transfer", "duplicate_payment" -> status == TransactionStatus.COMPLETED;
            case "refund_request" ->
                    status == TransactionStatus.REVERSED || status == TransactionStatus.COMPLETED;
            case "merchant_settlement_delay", "agent_cash_in_issue" -> status == TransactionStatus.PENDING;
            default -> status == TransactionStatus.COMPLETED;
        };
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }
}
