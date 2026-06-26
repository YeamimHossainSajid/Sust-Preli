package com.hackathon.investigator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standard API error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Error category", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable message", example = "Validation failed")
        String message,

        @Schema(description = "Request path", example = "/analyze-ticket")
        String path,

        @Schema(description = "Error timestamp")
        Instant timestamp,

        @Schema(description = "Field-level validation errors")
        List<FieldErrorDetail> fieldErrors
) {
    public record FieldErrorDetail(
            String field,
            String message,
            Object rejectedValue
    ) {
    }
}
