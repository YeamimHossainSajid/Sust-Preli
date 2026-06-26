package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.RiskLevel;
import com.hackathon.investigator.enums.Severity;
import com.hackathon.investigator.util.AmountSeverityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SeverityResolver {

    public Severity resolve(
            CaseType caseType,
            AiExtractionResult extraction,
            EvidenceVerdict evidenceVerdict,
            boolean ambiguousMatch,
            boolean establishedRecipientPattern,
            boolean vagueComplaint,
            TransactionRecord matchedTransaction,
            String complaint,
            List<TransactionRecord> transactions,
            boolean campaignContextPresent
    ) {
        if (vagueComplaint) {
            return Severity.LOW;
        }

        if (caseType == CaseType.PHISHING_OR_SOCIAL_ENGINEERING
                || extraction.riskLevel() == RiskLevel.CRITICAL) {
            return Severity.CRITICAL;
        }

        BigDecimal amount = AmountSeverityUtils.resolveInvestigationAmount(
                matchedTransaction,
                extraction,
                complaint,
                transactions
        );
        Severity severity = AmountSeverityUtils.severityFromAmount(amount);

        if (ambiguousMatch && severity == Severity.LOW) {
            severity = Severity.MEDIUM;
        }

        if (establishedRecipientPattern || evidenceVerdict == EvidenceVerdict.INCONSISTENT) {
            severity = maxSeverity(severity, Severity.MEDIUM);
        }

        return severity;
    }

    private Severity maxSeverity(Severity current, Severity floor) {
        return current.ordinal() >= floor.ordinal() ? current : floor;
    }
}
