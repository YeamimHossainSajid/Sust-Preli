package com.hackathon.investigator.util;

import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.EvidenceVerdict;

public final class ConfidenceCalculator {

    private ConfidenceCalculator() {
    }

    public static double calculate(
            CaseType caseType,
            EvidenceVerdict evidenceVerdict,
            boolean ambiguousMatch,
            boolean vagueComplaint,
            boolean establishedRecipientPattern,
            boolean duplicatePaymentScenario,
            boolean hasMatch
    ) {
        if (vagueComplaint) {
            return 0.6;
        }
        if (ambiguousMatch) {
            return 0.65;
        }
        if (caseType == CaseType.PHISHING_OR_SOCIAL_ENGINEERING) {
            return 0.95;
        }
        if (duplicatePaymentScenario) {
            return 0.93;
        }
        if (caseType == CaseType.MERCHANT_SETTLEMENT_DELAY && evidenceVerdict == EvidenceVerdict.CONSISTENT) {
            return 0.92;
        }
        if (caseType == CaseType.WRONG_TRANSFER && evidenceVerdict == EvidenceVerdict.CONSISTENT) {
            return 0.9;
        }
        if (caseType == CaseType.PAYMENT_FAILED && evidenceVerdict == EvidenceVerdict.CONSISTENT) {
            return 0.9;
        }
        if (caseType == CaseType.AGENT_CASH_IN_ISSUE && evidenceVerdict == EvidenceVerdict.CONSISTENT) {
            return 0.88;
        }
        if (caseType == CaseType.REFUND_REQUEST) {
            return 0.85;
        }
        if (establishedRecipientPattern) {
            return 0.75;
        }
        if (hasMatch && evidenceVerdict == EvidenceVerdict.CONSISTENT) {
            return 0.85;
        }
        return 0.7;
    }
}
