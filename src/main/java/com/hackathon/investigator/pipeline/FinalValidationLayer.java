package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.dto.DrafterPassOutput;
import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinalValidationLayer {

    public AnalyzeTicketResponse validateAndConstruct(
            AnalyzeTicketRequest request,
            InvestigatorPassOutput investigatorPass,
            DrafterPassOutput drafterPass,
            boolean flagged
    ) {
        EvidenceVerdict evidenceVerdict = parseEvidenceVerdict(investigatorPass.evidenceVerdict());
        CaseType caseType = parseCaseType(investigatorPass.caseType());
        Department department = parseDepartment(investigatorPass.department());
        Severity severity = parseSeverity(drafterPass.severity());

        List<String> reasonCodes = drafterPass.reasonCodes() == null || drafterPass.reasonCodes().isEmpty()
                ? List.of(caseType.getValue())
                : drafterPass.reasonCodes();

        double confidence = normalizeConfidence(drafterPass.confidence());
        boolean humanReviewRequired = drafterPass.humanReviewRequired() || flagged || confidence < 0.5;

        String agentSummary = defaultIfBlank(
                drafterPass.agentSummary(),
                "Customer complaint received for ticket " + request.ticketId() + "."
        );
        String recommendedNextAction = defaultIfBlank(
                drafterPass.recommendedNextAction(),
                "Review ticket " + request.ticketId() + " manually and follow the relevant SOP."
        );
        String customerReply = defaultIfBlank(
                drafterPass.customerReply(),
                "Thank you for contacting us. We have received your concern and our team is reviewing the details."
        );

        return new AnalyzeTicketResponse(
                request.ticketId(),
                investigatorPass.relevantTransactionId(),
                evidenceVerdict,
                caseType,
                severity,
                department,
                agentSummary,
                recommendedNextAction,
                customerReply,
                humanReviewRequired,
                confidence,
                reasonCodes
        );
    }

    private EvidenceVerdict parseEvidenceVerdict(String value) {
        try {
            return EvidenceVerdict.fromValue(value);
        } catch (IllegalArgumentException ex) {
            return EvidenceVerdict.INSUFFICIENT_DATA;
        }
    }

    private CaseType parseCaseType(String value) {
        try {
            return CaseType.fromValue(value);
        } catch (IllegalArgumentException ex) {
            return CaseType.OTHER;
        }
    }

    private Department parseDepartment(String value) {
        try {
            return Department.fromValue(value);
        } catch (IllegalArgumentException ex) {
            return Department.CUSTOMER_SUPPORT;
        }
    }

    private Severity parseSeverity(String value) {
        try {
            return Severity.fromValue(value);
        } catch (IllegalArgumentException ex) {
            return Severity.MEDIUM;
        }
    }

    private double normalizeConfidence(double confidence) {
        if (Double.isNaN(confidence) || Double.isInfinite(confidence)) {
            return 0.5;
        }
        return Math.max(0.0, Math.min(1.0, confidence));
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
