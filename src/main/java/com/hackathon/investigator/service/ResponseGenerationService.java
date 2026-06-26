package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;

import java.util.List;

public interface ResponseGenerationService {

    record GeneratedResponse(
            String agentSummary,
            String recommendedNextAction,
            String customerReply
    ) {
    }

    GeneratedResponse generate(
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
    );
}
