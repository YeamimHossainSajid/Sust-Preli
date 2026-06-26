package com.hackathon.investigator.service;

import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;

public interface EscalationService {
    boolean requiresHumanReview(
            CaseType caseType,
            EvidenceVerdict evidenceVerdict,
            Severity severity,
            boolean campaignContextPresent,
            boolean emptyTransactionHistory,
            boolean credentialsShared,
            double confidence
    );
}
