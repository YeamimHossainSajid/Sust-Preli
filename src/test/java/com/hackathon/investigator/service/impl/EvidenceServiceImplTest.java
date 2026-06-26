package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.RiskLevel;
import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceServiceImplTest {

    private final EvidenceServiceImpl service = new EvidenceServiceImpl();

    @Test
    void shouldReturnConsistentWhenPaymentFailedMatchesFailedTransaction() {
        TransactionRecord transaction = transaction(TransactionStatus.FAILED, TransactionType.PAYMENT);
        AiExtractionResult extraction = extraction("payment_failed");

        EvidenceVerdict verdict = service.evaluate(
                "My payment failed yesterday but balance was deducted",
                extraction,
                transaction,
                false,
                false,
                List.of(transaction)
        );

        assertThat(verdict).isEqualTo(EvidenceVerdict.CONSISTENT);
    }

    @Test
    void shouldReturnInconsistentWhenEstablishedRecipientPattern() {
        TransactionRecord transaction = transaction(TransactionStatus.COMPLETED, TransactionType.TRANSFER);

        EvidenceVerdict verdict = service.evaluate(
                "I sent 2000 to the wrong person by mistake",
                extraction("wrong_transfer"),
                transaction,
                true,
                false,
                List.of(transaction)
        );

        assertThat(verdict).isEqualTo(EvidenceVerdict.INCONSISTENT);
    }

    @Test
    void shouldReturnInsufficientDataWhenTransactionMissing() {
        EvidenceVerdict verdict = service.evaluate(
                "Payment failed",
                extraction("payment_failed"),
                null,
                false,
                false,
                List.of()
        );

        assertThat(verdict).isEqualTo(EvidenceVerdict.INSUFFICIENT_DATA);
    }

    @Test
    void shouldReturnInconsistentWhenDuplicateClaimHasOnlyOneTransaction() {
        TransactionRecord transaction = transaction(TransactionStatus.COMPLETED, TransactionType.PAYMENT);

        EvidenceVerdict verdict = service.evaluate(
                "I was charged twice for the same bill",
                extraction("duplicate_payment"),
                transaction,
                false,
                false,
                List.of(transaction)
        );

        assertThat(verdict).isEqualTo(EvidenceVerdict.INCONSISTENT);
    }

    private TransactionRecord transaction(TransactionStatus status, TransactionType type) {
        return TransactionRecord.builder()
                .transactionId("TXN-1")
                .timestamp(Instant.now())
                .type(type)
                .amount(new BigDecimal("500"))
                .counterparty("+8801711111111")
                .status(status)
                .build();
    }

    private AiExtractionResult extraction(String intent) {
        return new AiExtractionResult(intent, null, TransactionType.PAYMENT, RiskLevel.MEDIUM, List.of(), "rule-based");
    }
}
