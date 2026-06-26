package com.hackathon.investigator.pipeline.investigator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.investigator.client.OpenAiResponseClient;
import com.hackathon.investigator.config.AiProperties;
import com.hackathon.investigator.config.OpenAiProperties;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.exception.AiServiceException;
import com.hackathon.investigator.pipeline.AnalysisExecutionContext;
import com.hackathon.investigator.prompt.QueueStormPromptProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiInvestigatorPassService implements InvestigatorPassService {

    private static final String PASS_PROMPT = """
            You are QueueStorm Investigator Pass 1 (Level 1).
            Read the ticket JSON and return ONLY valid JSON with exactly these fields:
            {
              "relevant_transaction_id": "<string or null>",
              "evidence_verdict": "consistent|inconsistent|insufficient_data",
              "case_type": "wrong_transfer|payment_failed|refund_request|duplicate_payment|merchant_settlement_delay|agent_cash_in_issue|phishing_or_social_engineering|other",
              "department": "customer_support|dispute_resolution|payments_ops|merchant_operations|agent_operations|fraud_risk"
            }
            Follow all QueueStorm evidence and classification rules from the system prompt.
            """;

    private final OpenAiResponseClient openAiResponseClient;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;
    private final OpenAiProperties openAiProperties;
    private final QueueStormPromptProvider promptProvider;
    private final RuleBasedInvestigatorPassService ruleBasedInvestigatorPassService;

    public boolean isAvailable() {
        return aiProperties.isEnabled()
                && openAiProperties.getApiKey() != null
                && !openAiProperties.getApiKey().isBlank();
    }

    @Override
    public InvestigatorPassOutput execute(AnalysisExecutionContext context) {
        if (!isAvailable()) {
            return ruleBasedInvestigatorPassService.execute(context);
        }

        try {
            AnalyzeTicketRequest request = context.getRequest();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ticket_id", request.ticketId());
            payload.put("complaint", request.complaint());
            payload.put("language", request.language());
            payload.put("channel", request.channel());
            payload.put("user_type", request.userType());
            payload.put("campaign_context", request.campaignContext());
            payload.put("transaction_history", request.transactionHistory());

            JsonNode parsed = openAiResponseClient.requestJson(
                    aiProperties.getInvestigatorModel(),
                    promptProvider.getSystemPrompt() + "\n\n" + PASS_PROMPT,
                    objectMapper.writeValueAsString(payload)
            );

            String relevantTransactionId = parsed.path("relevant_transaction_id").isNull()
                    || parsed.path("relevant_transaction_id").asText().isBlank()
                    ? null
                    : parsed.path("relevant_transaction_id").asText();

            InvestigatorPassOutput output = new InvestigatorPassOutput(
                    relevantTransactionId,
                    parsed.path("evidence_verdict").asText("insufficient_data"),
                    parsed.path("case_type").asText("other"),
                    parsed.path("department").asText("customer_support")
            );

            context.applyInvestigatorPass(
                    EvidenceVerdict.fromValue(output.evidenceVerdict()),
                    CaseType.fromValue(output.caseType()),
                    Department.fromValue(output.department())
            );

            log.info(
                    "Investigator LLM pass completed for {} using {}",
                    request.ticketId(),
                    aiProperties.getInvestigatorModel()
            );

            return output;
        } catch (Exception ex) {
            log.warn("Investigator LLM pass failed for {}: {}", context.getRequest().ticketId(), ex.getMessage());
            throw new AiServiceException("Investigator LLM pass failed: " + ex.getMessage(), ex);
        }
    }
}
