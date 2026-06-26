package com.hackathon.investigator.util;

import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.EvidenceVerdict;

import java.util.ArrayList;
import java.util.List;

public final class ReasonCodeBuilder {

    private ReasonCodeBuilder() {
    }

    public static List<String> build(
            CaseType caseType,
            EvidenceVerdict evidenceVerdict,
            boolean ambiguousMatch,
            boolean vagueComplaint,
            boolean establishedRecipientPattern,
            boolean duplicatePaymentScenario,
            boolean campaignContextPresent,
            boolean adversarialComplaint
    ) {
        List<String> codes = new ArrayList<>();

        if (adversarialComplaint) {
            codes.add("adversarial_input");
            codes.add("needs_clarification");
            return codes;
        }

        if (ambiguousMatch) {
            codes.add("ambiguous_match");
            codes.add("needs_clarification");
            return codes;
        }

        if (vagueComplaint) {
            codes.add("vague_complaint");
            codes.add("needs_clarification");
            return codes;
        }

        switch (caseType) {
            case WRONG_TRANSFER -> {
                if (establishedRecipientPattern) {
                    codes.add("wrong_transfer_claim");
                    codes.add("established_recipient_pattern");
                    codes.add("evidence_inconsistent");
                } else {
                    codes.add("wrong_transfer");
                    codes.add("transaction_match");
                    codes.add("dispute_initiated");
                }
            }
            case PAYMENT_FAILED -> {
                codes.add("payment_failed");
                codes.add("potential_balance_deduction");
            }
            case REFUND_REQUEST -> {
                codes.add("refund_request");
                codes.add("merchant_policy_dependent");
            }
            case PHISHING_OR_SOCIAL_ENGINEERING -> {
                codes.add("phishing");
                codes.add("phishing_signal");
                codes.add("credential_protection");
                codes.add("critical_escalation");
            }
            case AGENT_CASH_IN_ISSUE -> {
                codes.add("agent_cash_in");
                codes.add("pending_transaction");
                codes.add("agent_ops");
            }
            case MERCHANT_SETTLEMENT_DELAY -> {
                codes.add("merchant_settlement");
                codes.add("delay");
                codes.add("pending");
            }
            case DUPLICATE_PAYMENT -> {
                codes.add("duplicate_payment");
                codes.add("duplicate_detected");
                codes.add("biller_verification_required");
            }
            case OTHER -> codes.add("general_inquiry");
        }

        if (evidenceVerdict == EvidenceVerdict.INCONSISTENT && !codes.contains("evidence_inconsistent")) {
            codes.add("evidence_inconsistent");
        }

        if (codes.isEmpty()) {
            codes.add(caseType.getValue());
        }

        return codes.stream().limit(4).toList();
    }
}
