package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.service.EvidenceService;
import com.hackathon.investigator.util.AmountSeverityUtils;
import com.hackathon.investigator.util.ComplaintAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EvidenceServiceImpl implements EvidenceService {

    @Override
    public EvidenceVerdict evaluate(
            String complaint,
            AiExtractionResult extraction,
            TransactionRecord transaction,
            boolean establishedRecipientPattern,
            boolean duplicatePaymentScenario,
            List<TransactionRecord> transactions
    ) {
        if (ComplaintAnalyzer.mentionsPhishing(complaint)) {
            return EvidenceVerdict.INSUFFICIENT_DATA;
        }

        if (ComplaintAnalyzer.mentionsDuplicatePayment(complaint) && !duplicatePaymentScenario) {
            long completedMatches = transactions == null ? 0 : transactions.stream()
                    .filter(tx -> tx.getStatus() == TransactionStatus.COMPLETED)
                    .count();
            if (completedMatches <= 1) {
                return EvidenceVerdict.INCONSISTENT;
            }
        }

        if (transaction == null) {
            return EvidenceVerdict.INSUFFICIENT_DATA;
        }

        if (establishedRecipientPattern) {
            return EvidenceVerdict.INCONSISTENT;
        }

        TransactionStatus status = transaction.getStatus();

        if (ComplaintAnalyzer.mentionsPaymentFailed(complaint)) {
            return status == TransactionStatus.FAILED ? EvidenceVerdict.CONSISTENT : EvidenceVerdict.INCONSISTENT;
        }

        if (ComplaintAnalyzer.mentionsDuplicatePayment(complaint)) {
            return status == TransactionStatus.COMPLETED ? EvidenceVerdict.CONSISTENT : EvidenceVerdict.INSUFFICIENT_DATA;
        }

        if (ComplaintAnalyzer.mentionsMerchantSettlement(complaint)) {
            if (status == TransactionStatus.PENDING) {
                return EvidenceVerdict.CONSISTENT;
            }
            if (status == TransactionStatus.COMPLETED) {
                return EvidenceVerdict.INCONSISTENT;
            }
        }

        if (ComplaintAnalyzer.mentionsAgentCashIn(complaint)) {
            if (status == TransactionStatus.PENDING) {
                return EvidenceVerdict.CONSISTENT;
            }
            if (status == TransactionStatus.COMPLETED) {
                return EvidenceVerdict.INCONSISTENT;
            }
        }

        if (ComplaintAnalyzer.mentionsRefundRequest(complaint)) {
            return status == TransactionStatus.COMPLETED ? EvidenceVerdict.CONSISTENT : EvidenceVerdict.INSUFFICIENT_DATA;
        }

        if (ComplaintAnalyzer.mentionsWrongTransfer(complaint) || ComplaintAnalyzer.mentionsNotReceived(complaint)) {
            if (status == TransactionStatus.COMPLETED) {
                return EvidenceVerdict.CONSISTENT;
            }
            if (status == TransactionStatus.FAILED || status == TransactionStatus.PENDING) {
                return EvidenceVerdict.INCONSISTENT;
            }
        }

        if (extraction.amount() != null
                && AmountSeverityUtils.amountWithinTolerance(transaction.getAmount(), extraction.amount())
                && extraction.transactionType() == transaction.getType()) {
            return EvidenceVerdict.CONSISTENT;
        }

        if (extraction.amount() != null
                && !AmountSeverityUtils.amountWithinTolerance(transaction.getAmount(), extraction.amount())) {
            return EvidenceVerdict.INCONSISTENT;
        }

        return EvidenceVerdict.INSUFFICIENT_DATA;
    }
}
