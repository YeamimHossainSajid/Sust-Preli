package com.hackathon.investigator.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the QueueStorm Investigator system prompt used for LLM-backed analysis.
 * The rule-based pipeline in {@code com.hackathon.investigator.service.impl} encodes the same rules.
 */
@Component
public class QueueStormPromptProvider {

    private static final String PROMPT_PATH = "prompts/queue-storm-system-prompt.txt";

    private volatile String cachedPrompt;

    public String getSystemPrompt() {
        if (cachedPrompt == null) {
            synchronized (this) {
                if (cachedPrompt == null) {
                    cachedPrompt = loadPrompt();
                }
            }
        }
        return cachedPrompt;
    }

    private String loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_PATH);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load QueueStorm system prompt from " + PROMPT_PATH, ex);
        }
    }
}
