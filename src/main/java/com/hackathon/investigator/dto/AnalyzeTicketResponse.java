package com.hackathon.investigator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ticket analysis response")
public record AnalyzeTicketResponse(
        @Schema(description = "Ticket identifier", example = "TKT-001")
        @JsonProperty("ticket_id")
        String ticketId,

        @Schema(description = "Most relevant matched transaction", example = "TXN-9101")
        @JsonProperty("relevant_transaction_id")
        String relevantTransactionId,

        @Schema(description = "Evidence consistency verdict")
        @JsonProperty("evidence_verdict")
        EvidenceVerdict evidenceVerdict,

        @Schema(description = "Classified case type")
        @JsonProperty("case_type")
        CaseType caseType,

        @Schema(description = "Case severity")
        Severity severity,

        @Schema(description = "Assigned department")
        Department department,

        @Schema(description = "Internal summary for support agents")
        @JsonProperty("agent_summary")
        String agentSummary,

        @Schema(description = "Recommended next action for operations")
        @JsonProperty("recommended_next_action")
        String recommendedNextAction,

        @Schema(description = "Safe customer-facing reply")
        @JsonProperty("customer_reply")
        String customerReply,

        @Schema(description = "Whether human review is required")
        @JsonProperty("human_review_required")
        boolean humanReviewRequired,

        @Schema(description = "Confidence score between 0 and 1", example = "0.90")
        double confidence,

        @Schema(description = "Machine-readable reason codes")
        @JsonProperty("reason_codes")
        List<String> reasonCodes
) {
}
