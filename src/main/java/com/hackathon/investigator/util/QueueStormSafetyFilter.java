package com.hackathon.investigator.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Post-generation safety filter aligned with QueueStorm integration notes.
 */
public final class QueueStormSafetyFilter {

    private static final List<String> FORBIDDEN_PHRASES = List.of(
            "your pin",
            "enter your otp",
            "share your password",
            "tell me your",
            "provide your mpin",
            "verify with your pin",
            "we will refund",
            "we will reverse",
            "your money will be returned",
            "we will credit",
            "your account will be unblocked",
            "contact this number",
            "visit this link",
            "call this agent"
    );

    private QueueStormSafetyFilter() {
    }

    public static List<String> findViolations(Map<String, Object> result) {
        List<String> violations = new ArrayList<>();
        checkField(violations, "customer_reply", stringValue(result.get("customer_reply")));
        checkField(violations, "recommended_next_action", stringValue(result.get("recommended_next_action")));
        return violations;
    }

    public static List<String> findViolations(String customerReply, String recommendedNextAction) {
        List<String> violations = new ArrayList<>();
        checkField(violations, "customer_reply", customerReply);
        checkField(violations, "recommended_next_action", recommendedNextAction);
        return violations;
    }

    private static void checkField(List<String> violations, String field, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        for (String phrase : FORBIDDEN_PHRASES) {
            if (normalized.contains(phrase)) {
                violations.add(field + " contains forbidden phrase: '" + phrase + "'");
            }
        }
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
