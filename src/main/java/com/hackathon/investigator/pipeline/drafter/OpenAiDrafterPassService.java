package com.hackathon.investigator.pipeline.drafter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.investigator.client.OpenAiResponseClient;
import com.hackathon.investigator.config.AiProperties;
import com.hackathon.investigator.config.OpenAiProperties;
import com.hackathon.investigator.dto.DrafterPassOutput;
import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Severity;
import com.hackathon.investigator.exception.AiServiceException;
import com.hackathon.investigator.pipeline.AnalysisExecutionContext;
import com.hackathon.investigator.prompt.QueueStormPromptProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiDrafterPassService implements DrafterPassService {

    private static final String PASS_PROMPT = """
            You are QueueStorm Investigator Pass 2 (Level 2 / Drafter).
            Input includes the original ticket and Pass 1 investigator output.
            Return ONLY valid JSON with exactly these fields:
            {
              "agent_summary": "<1-2 sentence internal summary>",
              "recommended_next_action": "<specific next step for the agent>",
              "customer_reply": "<safe English customer-facing reply>",
              "severity": "low|medium|high|critical",
              "human_review_required": <boolean>,
              "confidence": <number 0.0-1.0>,
              "reason_codes": ["<code>", "..."]
            }
            Follow all QueueStorm safety, severity, escalation, and reason_code rules from the system prompt.
            Use 2-4 short snake_case reason_codes matching the case (e.g. wrong_transfer, transaction_match, amount_mismatch).
            """;

    private final OpenAiResponseClient openAiResponseClient;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;
    private final OpenAiProperties openAiProperties;
    private final QueueStormPromptProvider promptProvider;
    private final RuleBasedDrafterPassService ruleBasedDrafterPassService;

    public boolean isAvailable() {
        return aiProperties.isEnabled()
                && openAiProperties.getApiKey() != null
                && !openAiProperties.getApiKey().isBlank();
    }

    @Override
    public DrafterPassOutput execute(AnalysisExecutionContext context, InvestigatorPassOutput investigatorPass) {
        if (!isAvailable()) {
            return ruleBasedDrafterPassService.execute(context, investigatorPass);
        }

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ticket", context.getRequest());
            payload.put("investigator_pass", investigatorPass);
            payload.put("flagged", context.isFlagged());

            JsonNode json = openAiResponseClient.requestJson(
                    aiProperties.getDrafterModel(),
                    promptProvider.getSystemPrompt() + "\n\n" + PASS_PROMPT,
                    objectMapper.writeValueAsString(payload)
            );

            List<String> reasonCodes = json.path("reason_codes").isArray()
                    ? objectMapper.convertValue(
                    json.path("reason_codes"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            )
                    : List.of(CaseType.fromValue(investigatorPass.caseType()).getValue());

            DrafterPassOutput output = new DrafterPassOutput(
                    json.path("agent_summary").asText(""),
                    json.path("recommended_next_action").asText(""),
                    json.path("customer_reply").asText(""),
                    json.path("severity").asText("medium"),
                    json.path("human_review_required").asBoolean(true),
                    json.path("confidence").asDouble(0.7),
                    reasonCodes
            );

            context.applyDrafterPass(
                    Severity.fromValue(output.severity()),
                    output.humanReviewRequired(),
                    output.confidence(),
                    output.reasonCodes(),
                    output.agentSummary(),
                    output.recommendedNextAction(),
                    output.customerReply()
            );

            log.info(
                    "Drafter LLM pass completed for {} using {}",
                    context.getRequest().ticketId(),
                    aiProperties.getDrafterModel()
            );

            return output;
        } catch (Exception ex) {
            log.warn("Drafter LLM pass failed for {}: {}", context.getRequest().ticketId(), ex.getMessage());
            throw new AiServiceException("Drafter LLM pass failed: " + ex.getMessage(), ex);
        }
    }
}
