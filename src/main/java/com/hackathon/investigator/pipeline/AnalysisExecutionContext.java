package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.TransactionMatchAnalysis;
import com.hackathon.investigator.entity.TransactionRecord;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalysisExecutionContext {

    private final AnalyzeTicketRequest request;
    private final boolean flagged;
    private final AiExtractionResult extraction;
    private final List<TransactionRecord> transactions;
    private final TransactionMatchAnalysis matchAnalysis;
    private final TransactionRecord matchedTransaction;
    private final boolean vagueComplaint;
    private final boolean credentialsShared;
    private final boolean emptyHistory;
    private final boolean campaignContextPresent;
    private final boolean ambiguousMatch;
    private final boolean establishedRecipientPattern;
    private final boolean duplicatePaymentScenario;
    private final boolean adversarialComplaint;

    private EvidenceVerdict evidenceVerdict;
    private CaseType caseType;
    private Department department;
    private Severity severity;
    private boolean humanReviewRequired;
    private double confidence;
    private List<String> reasonCodes;
    private String agentSummary;
    private String recommendedNextAction;
    private String customerReply;

    public void applyInvestigatorPass(
            EvidenceVerdict evidenceVerdict,
            CaseType caseType,
            Department department
    ) {
        this.evidenceVerdict = evidenceVerdict;
        this.caseType = caseType;
        this.department = department;
    }

    public void applyDrafterPass(
            Severity severity,
            boolean humanReviewRequired,
            double confidence,
            List<String> reasonCodes,
            String agentSummary,
            String recommendedNextAction,
            String customerReply
    ) {
        this.severity = severity;
        this.humanReviewRequired = humanReviewRequired;
        this.confidence = confidence;
        this.reasonCodes = reasonCodes;
        this.agentSummary = agentSummary;
        this.recommendedNextAction = recommendedNextAction;
        this.customerReply = customerReply;
    }

    public String relevantTransactionId() {
        return matchedTransaction != null ? matchedTransaction.getTransactionId() : null;
    }
}
