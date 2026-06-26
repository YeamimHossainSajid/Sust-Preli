package com.hackathon.investigator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "LLM Pass 1 (Investigator) output")
public record InvestigatorPassOutput(
        @JsonProperty("relevant_transaction_id")
        @Schema(description = "Matched transaction id or null")
        String relevantTransactionId,

        @JsonProperty("evidence_verdict")
        @Schema(description = "Evidence verdict", example = "consistent")
        String evidenceVerdict,

        @JsonProperty("case_type")
        @Schema(description = "Classified case type", example = "wrong_transfer")
        String caseType,

        @JsonProperty("department")
        @Schema(description = "Routing department", example = "dispute_resolution")
        String department
) {
}
