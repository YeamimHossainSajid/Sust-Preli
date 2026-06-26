package com.hackathon.investigator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Health check response")
public record HealthResponse(
        @Schema(description = "Service health status", example = "ok")
        String status
) {
    public static HealthResponse ok() {
        return new HealthResponse("ok");
    }
}
