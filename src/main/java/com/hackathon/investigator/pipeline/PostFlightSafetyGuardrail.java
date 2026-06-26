package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.dto.DrafterPassOutput;
import com.hackathon.investigator.service.SafetyService;
import com.hackathon.investigator.util.QueueStormSafetyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostFlightSafetyGuardrail {

    private static final String OFFICIAL_SUPPORT_GUIDANCE =
            "Please contact our official support channels for further assistance.";

    private final SafetyService safetyService;

    public DrafterPassOutput sanitize(DrafterPassOutput draft) {
        String agentSummary = safetyService.sanitizeAgentText(draft.agentSummary());
        String recommendedNextAction = sanitizeRecommendedAction(draft.recommendedNextAction());
        String customerReply = sanitizeCustomerReply(draft.customerReply());

        return new DrafterPassOutput(
                agentSummary,
                recommendedNextAction,
                customerReply,
                draft.severity(),
                draft.humanReviewRequired(),
                draft.confidence(),
                draft.reasonCodes()
        );
    }

    private String sanitizeRecommendedAction(String text) {
        String sanitized = safetyService.sanitizeAgentText(text);
        if (containsThirdPartyReferral(sanitized)) {
            return OFFICIAL_SUPPORT_GUIDANCE;
        }
        return sanitized;
    }

    private String sanitizeCustomerReply(String text) {
        String sanitized = safetyService.sanitizeCustomerReply(text);
        if (containsThirdPartyReferral(sanitized)) {
            sanitized = sanitized + " " + OFFICIAL_SUPPORT_GUIDANCE;
        }
        if (!QueueStormSafetyFilter.findViolations(sanitized, "").isEmpty()) {
            sanitized = safetyService.sanitizeCustomerReply(sanitized);
        }
        return sanitized;
    }

    private boolean containsThirdPartyReferral(String text) {
        if (text == null) {
            return false;
        }
        String normalized = text.toLowerCase();
        return normalized.contains("contact this number")
                || normalized.contains("visit this link")
                || normalized.contains("call this agent");
    }
}
