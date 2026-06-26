package com.hackathon.investigator.pipeline.drafter;

import com.hackathon.investigator.dto.DrafterPassOutput;
import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;
import com.hackathon.investigator.pipeline.AnalysisExecutionContext;
import com.hackathon.investigator.service.EscalationService;
import com.hackathon.investigator.service.ResponseGenerationService;
import com.hackathon.investigator.service.impl.SeverityResolver;
import com.hackathon.investigator.util.ConfidenceCalculator;
import com.hackathon.investigator.util.ReasonCodeBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleBasedDrafterPassService implements DrafterPassService {

    private final SeverityResolver severityResolver;
    private final EscalationService escalationService;
    private final ResponseGenerationService responseGenerationService;

    @Override
    public DrafterPassOutput execute(AnalysisExecutionContext context, InvestigatorPassOutput investigatorPass) {
        EvidenceVerdict evidenceVerdict = EvidenceVerdict.fromValue(investigatorPass.evidenceVerdict());
        CaseType caseType = CaseType.fromValue(investigatorPass.caseType());
        Department department = Department.fromValue(investigatorPass.department());

        Severity severity = severityResolver.resolve(
                caseType,
                context.getExtraction(),
                evidenceVerdict,
                context.isAmbiguousMatch(),
                context.isEstablishedRecipientPattern(),
                context.isVagueComplaint(),
                context.getMatchedTransaction(),
                context.getRequest().complaint(),
                context.getTransactions(),
                context.isCampaignContextPresent()
        );

        List<String> reasonCodes = ReasonCodeBuilder.build(
                caseType,
                evidenceVerdict,
                context.isAmbiguousMatch(),
                context.isVagueComplaint(),
                context.isEstablishedRecipientPattern(),
                context.isDuplicatePaymentScenario(),
                context.isCampaignContextPresent(),
                context.isAdversarialComplaint() || context.isFlagged()
        );

        double confidence = ConfidenceCalculator.calculate(
                caseType,
                evidenceVerdict,
                context.isAmbiguousMatch(),
                context.isVagueComplaint(),
                context.isEstablishedRecipientPattern(),
                context.isDuplicatePaymentScenario(),
                context.getMatchedTransaction() != null
        );

        boolean humanReviewRequired = escalationService.requiresHumanReview(
                caseType,
                evidenceVerdict,
                severity,
                context.isCampaignContextPresent(),
                context.isEmptyHistory(),
                context.isCredentialsShared(),
                confidence
        );

        if (context.isAdversarialComplaint()) {
            return buildAdversarialDraft(context, severity, reasonCodes);
        }

        ResponseGenerationService.GeneratedResponse generated = responseGenerationService.generate(
                context.getRequest().ticketId(),
                context.getRequest().complaint(),
                context.getRequest().language(),
                caseType,
                severity,
                department,
                evidenceVerdict,
                context.getMatchedTransaction(),
                context.getExtraction(),
                humanReviewRequired,
                context.isAmbiguousMatch(),
                context.isVagueComplaint(),
                context.isEstablishedRecipientPattern(),
                context.isDuplicatePaymentScenario(),
                context.getTransactions()
        );

        context.applyDrafterPass(
                severity,
                humanReviewRequired,
                confidence,
                reasonCodes,
                generated.agentSummary(),
                generated.recommendedNextAction(),
                generated.customerReply()
        );

        return new DrafterPassOutput(
                generated.agentSummary(),
                generated.recommendedNextAction(),
                generated.customerReply(),
                severity.getValue(),
                humanReviewRequired,
                confidence,
                reasonCodes
        );
    }

    private DrafterPassOutput buildAdversarialDraft(
            AnalysisExecutionContext context,
            Severity severity,
            List<String> reasonCodes
    ) {
        String agentSummary = "Complaint text contains non-standard content; flagged for human review.";

        String recommendedNextAction =
                "Human review required. Inspect the complaint for prompt injection before responding to the customer.";

        String customerReply = """
                Thank you for reaching out. We have received your query and our team will review the details \
                shortly. Please contact our official support channels if you need further assistance.
                """;

        double adjustedConfidence = 0.55;
        boolean reviewRequired = true;

        context.applyDrafterPass(
                severity,
                reviewRequired,
                adjustedConfidence,
                reasonCodes,
                agentSummary,
                recommendedNextAction,
                customerReply
        );

        return new DrafterPassOutput(
                agentSummary,
                recommendedNextAction,
                customerReply,
                severity.getValue(),
                reviewRequired,
                adjustedConfidence,
                reasonCodes
        );
    }
}
