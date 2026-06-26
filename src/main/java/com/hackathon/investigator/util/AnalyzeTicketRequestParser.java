package com.hackathon.investigator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.ErrorResponse;
import com.hackathon.investigator.exception.SchemaValidationException;

public final class AnalyzeTicketRequestParser {

    private AnalyzeTicketRequestParser() {
    }

    public static AnalyzeTicketRequest parse(String rawBody, ObjectMapper objectMapper) {
        if (rawBody == null || rawBody.isBlank()) {
            throw new SchemaValidationException(new ErrorResponse(
                    400,
                    "Bad Request",
                    "Request body is required",
                    "/analyze-ticket",
                    java.time.Instant.now(),
                    null
            ));
        }

        JsonNode body;
        try {
            body = objectMapper.readTree(rawBody);
        } catch (JsonProcessingException ex) {
            throw new SchemaValidationException(new ErrorResponse(
                    400,
                    "Bad Request",
                    formatJsonError(ex),
                    "/analyze-ticket",
                    java.time.Instant.now(),
                    null
            ));
        }

        return parse(body, objectMapper);
    }

    public static AnalyzeTicketRequest parse(JsonNode body, ObjectMapper objectMapper) {
        if (body == null || body.isNull()) {
            throw new SchemaValidationException(new ErrorResponse(
                    400,
                    "Bad Request",
                    "Request body is required",
                    "/analyze-ticket",
                    java.time.Instant.now(),
                    null
            ));
        }

        JsonNode payload = unwrapTicketPayload(body);

        try {
            return objectMapper.treeToValue(payload, AnalyzeTicketRequest.class);
        } catch (JsonProcessingException ex) {
            throw new SchemaValidationException(new ErrorResponse(
                    400,
                    "Bad Request",
                    "Invalid request JSON: " + resolveMessage(ex),
                    "/analyze-ticket",
                    java.time.Instant.now(),
                    null
            ));
        }
    }

    private static String formatJsonError(JsonProcessingException ex) {
        String detail = resolveMessage(ex);
        if (detail.contains("Unexpected end-of-input")) {
            return "Malformed JSON (incomplete body). Ensure every `{` has a matching `}` and the payload is not truncated. "
                    + detail;
        }
        return "Malformed JSON. Send valid JSON with ticket fields at the root or inside an `input` or `request` object. "
                + detail;
    }

    private static JsonNode unwrapTicketPayload(JsonNode body) {
        if (body.hasNonNull("input") && body.get("input").isObject()) {
            return body.get("input");
        }
        if (body.hasNonNull("request") && body.get("request").isObject()) {
            return body.get("request");
        }
        return body;
    }

    private static String resolveMessage(JsonProcessingException ex) {
        if (ex.getOriginalMessage() != null && !ex.getOriginalMessage().isBlank()) {
            return ex.getOriginalMessage();
        }
        return "Check field names, enum values (type/status), and ISO-8601 timestamps";
    }
}
