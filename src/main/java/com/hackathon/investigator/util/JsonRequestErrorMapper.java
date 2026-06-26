package com.hackathon.investigator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hackathon.investigator.dto.ErrorResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonRequestErrorMapper {

    private static final Pattern STRING_VALUE = Pattern.compile("String value \\('([^']+)'\\)");
    private static final Pattern FIELD_NAME = Pattern.compile("\\[\"([^\"]+)\"\\]");
    private static final Pattern ENUM_VALUE = Pattern.compile("not one of the values accepted for Enum class: ([\\w.]+)");

    private JsonRequestErrorMapper() {
    }

    public static FriendlyJsonError toFriendlyError(JsonProcessingException ex) {
        String technical = resolveTechnicalMessage(ex);
        String lower = technical.toLowerCase();

        if (lower.contains("unexpected end-of-input") || lower.contains("expected close marker")) {
            return new FriendlyJsonError(
                    "Incomplete JSON body. Check that every `{` and `[` has a matching closing bracket.",
                    List.of(bodyHint("The request was cut off or is missing closing braces."))
            );
        }

        if (lower.contains("unexpected character") || lower.contains("was expecting")) {
            return new FriendlyJsonError(
                    "Malformed JSON syntax. Use double quotes for keys and string values, and valid commas between fields.",
                    List.of(bodyHint("Fix JSON syntax before sending — invalid comma, quote, or bracket."))
            );
        }

        if (lower.contains("cannot construct instance")
                || lower.contains("no string-argument constructor")
                || lower.contains("cannot deserialize value of type")) {

            Matcher stringValue = STRING_VALUE.matcher(technical);
            if (stringValue.find()) {
                String token = stringValue.group(1);
                return new FriendlyJsonError(
                        "Invalid JSON structure near \"" + token + "\". Field names must be keys inside a JSON object, not standalone strings.",
                        List.of(bodyHint(
                                "Send a JSON object like {\"ticket_id\":\"TKT-001\",\"complaint\":\"...\", ...}. "
                                        + "Judge samples can use a \"request\" or \"input\" wrapper."
                        ))
                );
            }

            String field = extractFieldName(technical);
            if (field != null) {
                return new FriendlyJsonError(
                        "Wrong data type for field \"" + field + "\". Check that amounts are numbers, lists use [...], and objects use {...}.",
                        List.of(new ErrorResponse.FieldErrorDetail(
                                field,
                                friendlyTypeHint(field),
                                null
                        ))
                );
            }

            return new FriendlyJsonError(
                    "Invalid JSON types in the request. ticket_id, complaint, and other fields must be inside one JSON object with the correct types.",
                    List.of(bodyHint(
                            "Required shape: ticket_id (string), complaint (string), language, channel, user_type, transaction_history (array of objects)."
                    ))
            );
        }

        if (lower.contains("not one of the values accepted for enum")) {
            Matcher enumMatcher = ENUM_VALUE.matcher(technical);
            String field = extractFieldName(technical);
            String hint = enumMatcher.find()
                    ? "Use an allowed enum value for this field (see API docs / Swagger)."
                    : "Use an allowed enum value (see Swagger for type, status, channel, etc.).";
            return new FriendlyJsonError(
                    field != null
                            ? "Invalid value for \"" + field + "\". " + hint
                            : "One or more fields contain an invalid enum value. " + hint,
                    field != null
                            ? List.of(new ErrorResponse.FieldErrorDetail(field, hint, null))
                            : List.of(bodyHint(hint))
            );
        }

        if (lower.contains("cannot deserialize instance of `java.util.arraylist`")
                || lower.contains("out of start_array token")) {
            return new FriendlyJsonError(
                    "transaction_history must be a JSON array [ ... ] of transaction objects.",
                    List.of(new ErrorResponse.FieldErrorDetail(
                            "transaction_history",
                            "Expected an array. Each item needs transaction_id, timestamp, type, amount, counterparty, status.",
                            null
                    ))
            );
        }

        return new FriendlyJsonError(
                "Invalid request JSON. Send ticket fields as a JSON object at the root, or inside \"request\" / \"input\".",
                List.of(bodyHint(trimTechnical(technical)))
        );
    }

    private static ErrorResponse.FieldErrorDetail bodyHint(String message) {
        return new ErrorResponse.FieldErrorDetail("body", message, null);
    }

    private static String friendlyTypeHint(String field) {
        return switch (field) {
            case "transaction_history" -> "Must be an array of transaction objects.";
            case "amount" -> "Must be a number (e.g. 5000), not a string.";
            case "timestamp" -> "Must be an ISO-8601 UTC string (e.g. 2026-04-14T14:08:22Z).";
            case "ticket_id", "complaint", "language", "channel", "user_type", "campaign_context" ->
                    "Must be a text string in double quotes.";
            default -> "Check the field type in Swagger and use the sample request format.";
        };
    }

    private static String extractFieldName(String technical) {
        Matcher matcher = FIELD_NAME.matcher(technical);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (technical.contains("transaction_history")) {
            return "transaction_history";
        }
        if (technical.contains("AnalyzeTicketRequest")) {
            return "body";
        }
        return null;
    }

    private static String trimTechnical(String technical) {
        if (technical.length() <= 160) {
            return technical;
        }
        return technical.substring(0, 157) + "...";
    }

    private static String resolveTechnicalMessage(JsonProcessingException ex) {
        if (ex.getOriginalMessage() != null && !ex.getOriginalMessage().isBlank()) {
            return ex.getOriginalMessage();
        }
        return ex.getMessage() == null ? "Invalid JSON" : ex.getMessage();
    }

    public record FriendlyJsonError(String message, List<ErrorResponse.FieldErrorDetail> fieldErrors) {
    }
}
