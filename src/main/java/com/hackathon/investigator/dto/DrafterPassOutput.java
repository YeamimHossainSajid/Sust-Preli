package com.hackathon.investigator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "LLM Pass 2 (Drafter) output")
public record DrafterPassOutput(
        @JsonProperty("agent_summary")
        String agentSummary,

        @JsonProperty("recommended_next_action")
        String recommendedNextAction,

        @JsonProperty("customer_reply")
        String customerReply,

        @Schema(description = "Case severity", example = "high")
        String severity,

        @JsonProperty("human_review_required")
        boolean humanReviewRequired,

        @Schema(description = "Confidence score 0.0-1.0", example = "0.9")
        double confidence,

        @JsonProperty("reason_codes")
        List<String> reasonCodes
) {
}
