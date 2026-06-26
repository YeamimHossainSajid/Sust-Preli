package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.EvidenceVerdict;

import java.util.List;

public interface EvidenceService {
    EvidenceVerdict evaluate(
            String complaint,
            AiExtractionResult extraction,
            TransactionRecord transaction,
            boolean establishedRecipientPattern,
            boolean duplicatePaymentScenario,
            List<TransactionRecord> transactions
    );
}
