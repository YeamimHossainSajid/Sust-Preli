package com.hackathon.investigator.prompt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueStormPromptProviderTest {

    @Autowired
    private QueueStormPromptProvider promptProvider;

    @Test
    void shouldLoadQueueStormSystemPrompt() {
        String prompt = promptProvider.getSystemPrompt();

        assertThat(prompt).contains("QueueStorm Investigator");
        assertThat(prompt).contains("evidence_verdict");
        assertThat(prompt).contains("human_review_required");
    }
}
