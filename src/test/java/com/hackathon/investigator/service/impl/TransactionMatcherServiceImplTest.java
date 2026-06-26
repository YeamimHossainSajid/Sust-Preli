package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.dto.TransactionMatchAnalysis;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.RiskLevel;
import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMatcherServiceImplTest {

    private final TransactionMatcherServiceImpl service = new TransactionMatcherServiceImpl();

    @Test
    void shouldReturnHighestScoringTransaction() {
        TransactionRecord bestMatch = TransactionRecord.builder()
                .transactionId("TXN-9101")
                .timestamp(Instant.parse("2026-04-14T14:08:22Z"))
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("5000"))
                .counterparty("+8801719876543")
                .status(TransactionStatus.COMPLETED)
                .build();

        TransactionRecord other = TransactionRecord.builder()
                .transactionId("TXN-0001")
                .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                .type(TransactionType.PAYMENT)
                .amount(new BigDecimal("100"))
                .counterparty("+8801000000000")
                .status(TransactionStatus.FAILED)
                .build();

        AiExtractionResult extraction = new AiExtractionResult(
                "wrong_transfer",
                new BigDecimal("5000"),
                TransactionType.TRANSFER,
                RiskLevel.HIGH,
                List.of("wrong", "transfer"),
                "rule-based"
        );

        TransactionMatchAnalysis result = service.analyzeMatches(
                List.of(other, bestMatch),
                extraction,
                "I sent 5000 taka to +8801719876543 by mistake"
        );

        assertThat(result.matchedTransaction()).isNotNull();
        assertThat(result.matchedTransaction().getTransactionId()).isEqualTo("TXN-9101");
        assertThat(result.matchScore()).isGreaterThan(5);
        assertThat(result.ambiguousMatch()).isFalse();
    }

    @Test
    void shouldDetectAmbiguousMatchesWhenMultipleTransactionsTie() {
        List<TransactionRecord> transactions = List.of(
                transaction("TXN-1", "+8801711111111", TransactionStatus.COMPLETED),
                transaction("TXN-2", "+8801722222222", TransactionStatus.COMPLETED),
                transaction("TXN-3", "+8801733333333", TransactionStatus.FAILED)
        );

        AiExtractionResult extraction = new AiExtractionResult(
                "wrong_transfer",
                new BigDecimal("1000"),
                TransactionType.TRANSFER,
                RiskLevel.MEDIUM,
                List.of("brother", "transfer"),
                "rule-based"
        );

        TransactionMatchAnalysis result = service.analyzeMatches(
                transactions,
                extraction,
                "I sent 1000 taka to my brother but he has not received it"
        );

        assertThat(result.ambiguousMatch()).isTrue();
        assertThat(result.matchedTransaction()).isNull();
        assertThat(result.plausibleMatchCount()).isEqualTo(3);
    }

    private TransactionRecord transaction(String id, String counterparty, TransactionStatus status) {
        return TransactionRecord.builder()
                .transactionId(id)
                .timestamp(Instant.parse("2026-04-14T10:00:00Z"))
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("1000"))
                .counterparty(counterparty)
                .status(status)
                .build();
    }
}
