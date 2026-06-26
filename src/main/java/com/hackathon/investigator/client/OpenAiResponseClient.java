package com.hackathon.investigator.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiResponseClient {

    private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*(\\{[\\s\\S]*?})\\s*```");

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    public JsonNode requestJson(String model, String instructions, String userInput) {
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .instructions(instructions)
                .input(userInput)
                .build();

        log.debug("Calling OpenAI Responses API with model={}", model);
        Response response = openAIClient.responses().create(params);
        String outputText = extractOutputText(response);

        if (outputText == null || outputText.isBlank()) {
            throw new IllegalStateException("OpenAI returned empty output");
        }

        return parseJsonOutput(outputText);
    }

    private String extractOutputText(Response response) {
        StringBuilder builder = new StringBuilder();
        for (ResponseOutputItem item : response.output()) {
            item.message().ifPresent(message -> appendMessageText(builder, message));
        }
        return builder.toString();
    }

    private void appendMessageText(StringBuilder builder, ResponseOutputMessage message) {
        for (ResponseOutputMessage.Content content : message.content()) {
            if (content.isOutputText()) {
                builder.append(content.asOutputText().text());
            }
        }
    }

    private JsonNode parseJsonOutput(String outputText) {
        String trimmed = outputText.trim();
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception ignored) {
            Matcher matcher = JSON_BLOCK.matcher(trimmed);
            if (matcher.find()) {
                try {
                    return objectMapper.readTree(matcher.group(1));
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to parse JSON from fenced OpenAI output", ex);
                }
            }
            throw new IllegalStateException("OpenAI output was not valid JSON: " + trimmed);
        }
    }
}
