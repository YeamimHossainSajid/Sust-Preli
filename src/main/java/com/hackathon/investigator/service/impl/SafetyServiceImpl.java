package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.service.SafetyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class SafetyServiceImpl implements SafetyService {

    private static final List<Pattern> UNSAFE_PATTERNS = List.of(
            Pattern.compile("\\b(share|provide|send|enter|give|tell me).{0,24}\\botp\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(share|provide|send|enter|give|tell me).{0,24}\\bpin\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(share|provide|send|enter|give|tell me).{0,24}\\bpassword\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(share|provide|send|enter|give|tell me).{0,24}\\bmpin\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(share|provide|send|enter|give).{0,24}\\bcard\\s*number\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(we\\s+will\\s+refund|we\\s+will\\s+reverse|your\\s+money\\s+will\\s+be\\s+returned)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(we\\s+will\\s+credit|your\\s+account\\s+will\\s+be\\s+unblocked)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(refund\\s+(is\\s+)?guaranteed|money\\s+will\\s+be\\s+refunded)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(account\\s+(is\\s+)?recovered|we\\s+recovered\\s+your\\s+account)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(money\\s+will\\s+be\\s+reversed|funds\\s+will\\s+be\\s+returned\\s+immediately)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(contact this number|visit this link|call this agent)\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final String SAFE_CUSTOMER_FALLBACK =
            "Thank you for contacting us. We have received your concern and our team is reviewing the details. "
                    + "Please do not share your PIN, OTP, or password with anyone.";

    private static final String SAFE_AGENT_FALLBACK =
            "Review the ticket details manually and follow the relevant standard operating procedure.";

    @Override
    public String sanitizeCustomerReply(String text) {
        return sanitize(text, SAFE_CUSTOMER_FALLBACK);
    }

    @Override
    public String sanitizeAgentText(String text) {
        return sanitize(text, SAFE_AGENT_FALLBACK);
    }

    private String sanitize(String text, String fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        if (isUnsafe(text)) {
            return fallback;
        }
        return text.trim();
    }

    @Override
    public boolean isUnsafe(String text) {
        if (text == null) {
            return false;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        if (isSecurityWarning(normalized)) {
            return false;
        }
        return UNSAFE_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(normalized).find());
    }

    private boolean isSecurityWarning(String text) {
        return text.contains("do not share")
                || text.contains("don't share")
                || text.contains("never share")
                || text.contains("please do not share")
                || text.contains("never ask for");
    }
}
