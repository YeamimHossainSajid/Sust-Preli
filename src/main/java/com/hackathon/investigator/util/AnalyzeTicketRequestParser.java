package com.hackathon.investigator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.ErrorResponse;
import com.hackathon.investigator.exception.SchemaValidationException;

import java.util.List;

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
            JsonRequestErrorMapper.FriendlyJsonError friendly = JsonRequestErrorMapper.toFriendlyError(ex);
            throw new SchemaValidationException(new ErrorResponse(
                    400,
                    "Bad Request",
                    friendly.message(),
                    "/analyze-ticket",
                    java.time.Instant.now(),
                    friendly.fieldErrors()
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

        if (!payload.isObject()) {
            throw new SchemaValidationException(new ErrorResponse(
                    400,
                    "Bad Request",
                    "Invalid JSON structure. Send a JSON object with ticket fields, not a plain string or array.",
                    "/analyze-ticket",
                    java.time.Instant.now(),
                    List.of(new ErrorResponse.FieldErrorDetail(
                            "body",
                            "Use {\"ticket_id\":\"...\",\"complaint\":\"...\", ...} or wrap fields in \"request\" / \"input\".",
                            null
                    ))
            ));
        }

        try {
            return objectMapper.treeToValue(payload, AnalyzeTicketRequest.class);
        } catch (JsonProcessingException ex) {
            JsonRequestErrorMapper.FriendlyJsonError friendly = JsonRequestErrorMapper.toFriendlyError(ex);
            throw new SchemaValidationException(new ErrorResponse(
                    400,
                    "Bad Request",
                    friendly.message(),
                    "/analyze-ticket",
                    java.time.Instant.now(),
                    friendly.fieldErrors()
            ));
        }
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
}
