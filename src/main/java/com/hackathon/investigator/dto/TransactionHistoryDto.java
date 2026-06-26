package com.hackathon.investigator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Historical transaction record")
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionHistoryDto(
        @Schema(description = "Transaction identifier", example = "TXN-9101")
        @JsonProperty("transaction_id")
        @NotBlank(message = "transaction_id is required")
        String transactionId,

        @Schema(description = "Transaction timestamp in ISO-8601 UTC", example = "2026-04-14T14:08:22Z")
        @NotNull(message = "timestamp is required")
        Instant timestamp,

        @Schema(description = "Transaction type", example = "transfer")
        @NotNull(message = "type is required")
        TransactionType type,

        @Schema(description = "Transaction amount", example = "5000")
        @NotNull(message = "amount is required")
        @PositiveOrZero(message = "amount must be zero or positive")
        BigDecimal amount,

        @Schema(description = "Counterparty identifier", example = "+8801719876543")
        @NotBlank(message = "counterparty is required")
        String counterparty,

        @Schema(description = "Transaction status", example = "completed")
        @NotNull(message = "status is required")
        TransactionStatus status
) {
}
