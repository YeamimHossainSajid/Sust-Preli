package com.hackathon.investigator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Ticket analysis request payload")
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalyzeTicketRequest(
        @Schema(description = "Unique ticket identifier", example = "TKT-001")
        @JsonProperty("ticket_id")
        @NotBlank(message = "ticket_id is required")
        @Size(max = 64, message = "ticket_id must not exceed 64 characters")
        String ticketId,

        @Schema(description = "Customer complaint text", example = "I sent 5000 taka to the wrong number.")
        @NotBlank(message = "complaint is required")
        @Size(max = 5000, message = "complaint must not exceed 5000 characters")
        String complaint,

        @Schema(description = "Complaint language code", example = "en", allowableValues = {"en", "bn", "mixed"})
        @Pattern(regexp = "^(|en|bn|mixed|banglish)$", message = "language must be en, bn, or mixed")
        String language,

        @Schema(description = "Support channel", example = "in_app_chat")
        @JsonProperty("channel")
        @Pattern(
                regexp = "^(|in_app_chat|call_center|email|merchant_portal|field_agent)$",
                message = "channel must be one of: in_app_chat, call_center, email, merchant_portal, field_agent"
        )
        String channel,

        @Schema(description = "User type submitting the complaint", example = "customer")
        @JsonProperty("user_type")
        @Pattern(
                regexp = "^(|customer|merchant|agent|unknown)$",
                message = "user_type must be one of: customer, merchant, agent, unknown"
        )
        String userType,

        @Schema(description = "Optional campaign identifier (elevates fraud review requirements)", example = "boishakh_bonanza_day_1")
        @JsonProperty("campaign_context")
        String campaignContext,

        @Schema(description = "Recent transaction history for matching")
        @JsonProperty("transaction_history")
        @Valid
        List<TransactionHistoryDto> transactionHistory
) {
    public AnalyzeTicketRequest normalized() {
        return new AnalyzeTicketRequest(
                ticketId,
                complaint,
                normalizeLanguage(language),
                normalize(channel, "in_app_chat"),
                normalize(userType, "customer"),
                normalizeOptional(campaignContext),
                transactionHistory == null ? List.of() : transactionHistory
        );
    }

    public boolean hasCampaignContext() {
        return campaignContext != null && !campaignContext.isBlank();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String normalizeLanguage(String value) {
        if (value == null || value.isBlank()) {
            return "en";
        }
        if ("banglish".equalsIgnoreCase(value)) {
            return "mixed";
        }
        return value.toLowerCase();
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.toLowerCase();
    }
}
