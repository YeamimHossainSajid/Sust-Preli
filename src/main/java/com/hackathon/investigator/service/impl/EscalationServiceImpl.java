package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;
import com.hackathon.investigator.service.EscalationService;
import org.springframework.stereotype.Service;

@Service
public class EscalationServiceImpl implements EscalationService {

    @Override
    public boolean requiresHumanReview(
            CaseType caseType,
            EvidenceVerdict evidenceVerdict,
            Severity severity,
            boolean campaignContextPresent,
            boolean emptyTransactionHistory,
            boolean credentialsShared,
            double confidence
    ) {
        if (campaignContextPresent || emptyTransactionHistory || credentialsShared) {
            return true;
        }

        if (severity == Severity.CRITICAL || severity == Severity.HIGH) {
            return true;
        }

        if (evidenceVerdict == EvidenceVerdict.INCONSISTENT) {
            return true;
        }

        if (caseType == CaseType.WRONG_TRANSFER
                || caseType == CaseType.PHISHING_OR_SOCIAL_ENGINEERING
                || caseType == CaseType.DUPLICATE_PAYMENT) {
            return true;
        }

        if (caseType == CaseType.AGENT_CASH_IN_ISSUE && evidenceVerdict == EvidenceVerdict.CONSISTENT) {
            return true;
        }

        if (confidence < 0.5) {
            return true;
        }

        if (severity == Severity.LOW || severity == Severity.MEDIUM) {
            if (evidenceVerdict == EvidenceVerdict.CONSISTENT
                    || evidenceVerdict == EvidenceVerdict.INSUFFICIENT_DATA) {
                return !isLowTouchCase(caseType, evidenceVerdict);
            }
        }

        return true;
    }

    private boolean isLowTouchCase(CaseType caseType, EvidenceVerdict evidenceVerdict) {
        return switch (caseType) {
            case OTHER, REFUND_REQUEST, PAYMENT_FAILED, AGENT_CASH_IN_ISSUE -> true;
            case MERCHANT_SETTLEMENT_DELAY -> evidenceVerdict == EvidenceVerdict.CONSISTENT;
            default -> false;
        };
    }
}
