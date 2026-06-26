package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;
import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.service.ResponseGenerationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ResponseGenerationServiceImpl implements ResponseGenerationService {

    @Override
    public GeneratedResponse generate(
            String ticketId,
            String complaint,
            String language,
            CaseType caseType,
            Severity severity,
            Department department,
            EvidenceVerdict evidenceVerdict,
            TransactionRecord transaction,
            AiExtractionResult extraction,
            boolean humanReviewRequired,
            boolean ambiguousMatch,
            boolean vagueComplaint,
            boolean establishedRecipientPattern,
            boolean duplicatePaymentScenario,
            List<TransactionRecord> transactions
    ) {
        GeneratedResponse response = switch (caseType) {
            case WRONG_TRANSFER -> generateWrongTransferResponse(
                    transaction, extraction, ambiguousMatch, establishedRecipientPattern, transactions
            );
            case PAYMENT_FAILED -> generatePaymentFailedResponse(transaction);
            case REFUND_REQUEST -> generateRefundResponse(transaction);
            case DUPLICATE_PAYMENT -> generateDuplicatePaymentResponse(transaction, transactions);
            case MERCHANT_SETTLEMENT_DELAY -> generateMerchantSettlementResponse(transaction);
            case AGENT_CASH_IN_ISSUE -> generateAgentCashInResponse(transaction, language);
            case PHISHING_OR_SOCIAL_ENGINEERING -> generatePhishingResponse();
            case OTHER -> generateVagueResponse(vagueComplaint);
        };

        return new GeneratedResponse(
                response.agentSummary(),
                response.recommendedNextAction(),
                response.customerReply()
        );
    }

    private GeneratedResponse generateWrongTransferResponse(
            TransactionRecord transaction,
            AiExtractionResult extraction,
            boolean ambiguousMatch,
            boolean establishedRecipientPattern,
            List<TransactionRecord> transactions
    ) {
        if (ambiguousMatch) {
            BigDecimal amount = resolveAmount(extraction, transactions);
            long completed = countByAmountAndStatus(transactions, amount, TransactionStatus.COMPLETED);
            long failed = countByAmountAndStatus(transactions, amount, TransactionStatus.FAILED);

            return new GeneratedResponse(
                    """
                    Customer reports a %s BDT transfer to their brother was not received. \
                    %d transactions of %s BDT exist on the date in question (%d completed, %d failed) \
                    to two different recipients. Cannot determine which is the brother's number without further input.
                    """.formatted(
                            formatAmount(amount),
                            transactions.size(),
                            formatAmount(amount),
                            completed,
                            failed
                    ).trim(),
                    "Reply to customer asking for the brother's number to identify the correct transaction. "
                            + "Do not initiate dispute until the transaction is confirmed.",
                    """
                    Thank you for reaching out. We see multiple transactions of %s BDT on that date. \
                    Could you share your brother's number so we can identify the right transaction? \
                    Please do not share your PIN or OTP with anyone.
                    """.formatted(formatAmount(amount)).trim()
            );
        }

        String txnId = transaction.getTransactionId();
        String counterparty = transaction.getCounterparty();
        String amount = formatAmount(transaction.getAmount());

        if (establishedRecipientPattern) {
            long priorCount = transactions.stream()
                    .filter(tx -> tx.getCounterparty().equals(counterparty))
                    .count();

            return new GeneratedResponse(
                    """
                    Customer claims %s (%s BDT to %s) was a wrong transfer, but transaction history shows \
                    %d prior transfers to the same counterparty, suggesting an established recipient.
                    """.formatted(txnId, amount, counterparty, Math.max(priorCount - 1, 1)).trim(),
                    "Flag for human review. Verify with the customer whether this was genuinely a wrong transfer "
                            + "given the established transaction pattern with this recipient.",
                    """
                    We have received your request regarding transaction %s. Please do not share your PIN or OTP with anyone. \
                    Our dispute team will review the case carefully and contact you through official support channels.
                    """.formatted(txnId).trim()
            );
        }

        return new GeneratedResponse(
                """
                Customer reports sending %s BDT via %s to %s, which they now believe was the wrong recipient. \
                Recipient is unresponsive.
                """.formatted(amount, txnId, counterparty).trim(),
                "Verify " + txnId + " details with the customer and initiate the wrong-transfer dispute workflow per policy.",
                """
                We have noted your concern about transaction %s. Please do not share your PIN or OTP with anyone. \
                Our dispute team will review the case and contact you through official support channels.
                """.formatted(txnId).trim()
        );
    }

    private GeneratedResponse generatePaymentFailedResponse(TransactionRecord transaction) {
        String txnId = transaction.getTransactionId();
        return new GeneratedResponse(
                """
                Customer attempted a %s BDT mobile recharge (%s) which failed, but reports balance was deducted. \
                Requires payments operations investigation.
                """.formatted(formatAmount(transaction.getAmount()), txnId).trim(),
                "Investigate " + txnId + " ledger status. If balance was deducted on a failed payment, "
                        + "initiate the automatic reversal flow within standard SLA.",
                """
                We have noted that transaction %s may have caused an unexpected balance deduction. \
                Our payments team will review the case and any eligible amount will be returned through official channels. \
                Please do not share your PIN or OTP with anyone.
                """.formatted(txnId).trim()
        );
    }

    private GeneratedResponse generateRefundResponse(TransactionRecord transaction) {
        String txnId = transaction != null ? transaction.getTransactionId() : "the transaction";
        String amount = transaction != null ? formatAmount(transaction.getAmount()) : "the payment amount";
        return new GeneratedResponse(
                "Customer requests refund of %s BDT for %s (merchant payment) due to change of mind. Not a service failure."
                        .formatted(amount, txnId),
                "Inform the customer that refund eligibility depends on the merchant's own policy. "
                        + "Provide guidance on contacting the merchant directly for a refund.",
                """
                Thank you for reaching out. Refunds for completed merchant payments depend on the merchant's own policy. \
                We recommend contacting the merchant directly. If you need help reaching them, please reply and we will guide you. \
                Please do not share your PIN or OTP with anyone.
                """.trim()
        );
    }

    private GeneratedResponse generateDuplicatePaymentResponse(
            TransactionRecord transaction,
            List<TransactionRecord> transactions
    ) {
        String duplicateId = transaction.getTransactionId();
        String firstId = transactions.stream()
                .filter(tx -> tx.getAmount().compareTo(transaction.getAmount()) == 0)
                .filter(tx -> tx.getCounterparty().equals(transaction.getCounterparty()))
                .map(TransactionRecord::getTransactionId)
                .filter(id -> !id.equals(duplicateId))
                .findFirst()
                .orElse("prior transaction");

        return new GeneratedResponse(
                """
                Customer reports duplicate electricity bill payment. Two identical %s BDT payments to %s were completed \
                (%s and %s). The second is likely the duplicate.
                """.formatted(
                        formatAmount(transaction.getAmount()),
                        transaction.getCounterparty(),
                        firstId,
                        duplicateId
                ).trim(),
                "Verify the duplicate with payments_ops. If the biller confirms only one payment was received, "
                        + "initiate reversal of " + duplicateId + ".",
                """
                We have noted the possible duplicate payment for transaction %s. Our payments team will verify with the biller \
                and any eligible amount will be returned through official channels. Please do not share your PIN or OTP with anyone.
                """.formatted(duplicateId).trim()
        );
    }

    private GeneratedResponse generateMerchantSettlementResponse(TransactionRecord transaction) {
        String txnId = transaction.getTransactionId();
        return new GeneratedResponse(
                """
                Merchant reports yesterday's %s BDT settlement (%s) is delayed beyond the standard 11 AM next-day window. \
                Settlement status is pending.
                """.formatted(formatAmount(transaction.getAmount()), txnId).trim(),
                "Route to merchant_operations to verify settlement batch status. "
                        + "If the batch is delayed, communicate a revised ETA to the merchant.",
                """
                We have noted your concern about settlement %s. Our merchant operations team will check the batch status \
                and update you on the expected settlement time through official channels.
                """.formatted(txnId).trim()
        );
    }

    private GeneratedResponse generateAgentCashInResponse(TransactionRecord transaction, String language) {
        String txnId = transaction.getTransactionId();
        String agentSummary = """
                Customer reports %s BDT cash-in via %s (%s) not reflected in balance. \
                Transaction status is pending. Agent claims funds were sent.
                """.formatted(formatAmount(transaction.getAmount()), transaction.getCounterparty(), txnId).trim();

        String recommended = "Investigate " + txnId + " pending status with agent operations. "
                + "Confirm settlement state and resolve within the standard cash-in SLA.";

        String customerReply = """
                Thank you for reporting this. We have escalated your case to the relevant team for review. \
                You will receive an update within the standard resolution window for transaction %s. \
                Please retain your transaction receipts for reference. \
                Please do not share your PIN, OTP, or password with anyone.
                """.formatted(txnId).trim();

        return new GeneratedResponse(agentSummary, recommended, customerReply);
    }

    private GeneratedResponse generatePhishingResponse() {
        return new GeneratedResponse(
                """
                Customer reports an unsolicited call claiming to be from the company and asking for OTP. \
                Customer has not yet shared credentials. Likely social engineering attempt.
                """.trim(),
                "Escalate to fraud_risk team immediately. Confirm to customer that the company never asks for OTP. "
                        + "Log the reported number for fraud pattern analysis.",
                """
                We are sorry to hear you received a suspicious call. Please do NOT share your PIN, OTP, password, \
                or any personal information with anyone claiming to represent our platform. Our agents will NEVER ask \
                for your credentials. We have flagged this report for our security team and will follow up. \
                If you believe your account may be compromised, please contact our official helpline immediately.
                """.trim()
        );
    }

    private GeneratedResponse generateVagueResponse(boolean vagueComplaint) {
        return new GeneratedResponse(
                """
                Customer reports a vague concern about their money without specifying transaction, amount, or issue. \
                Insufficient detail to identify any relevant transaction.
                """.trim(),
                "Reply to customer asking for specific details: which transaction, what amount, what went wrong, and approximate time.",
                """
                Thank you for reaching out. To help you faster, please share the transaction ID, the amount involved, \
                and a short description of what went wrong. Please do not share your PIN or OTP with anyone.
                """.trim()
        );
    }

    private BigDecimal resolveAmount(AiExtractionResult extraction, List<TransactionRecord> transactions) {
        if (extraction.amount() != null) {
            return extraction.amount();
        }
        return transactions.stream().map(TransactionRecord::getAmount).findFirst().orElse(BigDecimal.ZERO);
    }

    private long countByAmountAndStatus(
            List<TransactionRecord> transactions,
            BigDecimal amount,
            TransactionStatus status
    ) {
        return transactions.stream()
                .filter(tx -> tx.getAmount().compareTo(amount) == 0)
                .filter(tx -> tx.getStatus() == status)
                .count();
    }

    private String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}
