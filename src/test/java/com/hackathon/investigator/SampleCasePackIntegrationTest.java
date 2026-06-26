package com.hackathon.investigator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SampleCasePackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestFactory
    Stream<DynamicTest> shouldMatchSampleCasePackExpectations() throws Exception {
        JsonNode root = objectMapper.readTree(
                getClass().getResourceAsStream("/sample-case-pack.json")
        );

        return StreamSupport.stream(root.path("cases").spliterator(), false)
                .map(caseNode -> DynamicTest.dynamicTest(
                        caseNode.path("id").asText() + " - " + caseNode.path("label").asText(),
                        () -> assertCase(caseNode)
                ));
    }

    private void assertCase(JsonNode caseNode) throws Exception {
        JsonNode expected = caseNode.path("expected_output");
        String payload = caseNode.toString();

        MvcResult result = mockMvc.perform(post("/analyze-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode actual = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertThat(actual.path("ticket_id").asText()).isEqualTo(expected.path("ticket_id").asText());
        assertJsonNullOrEqual(actual.path("relevant_transaction_id"), expected.path("relevant_transaction_id"));
        assertThat(actual.path("evidence_verdict").asText()).isEqualTo(expected.path("evidence_verdict").asText());
        assertThat(actual.path("case_type").asText()).isEqualTo(expected.path("case_type").asText());
        assertThat(actual.path("severity").asText()).isEqualTo(expected.path("severity").asText());
        assertThat(actual.path("department").asText()).isEqualTo(expected.path("department").asText());
        assertThat(actual.path("human_review_required").asBoolean())
                .isEqualTo(expected.path("human_review_required").asBoolean());
        assertThat(actual.path("confidence").asDouble()).isEqualTo(expected.path("confidence").asDouble());

        for (JsonNode code : expected.path("reason_codes")) {
            assertThat(actual.path("reason_codes").toString()).contains(code.asText());
        }

        assertThat(actual.path("customer_reply").asText()).isNotBlank();
        assertThat(actual.path("agent_summary").asText()).isNotBlank();
        assertThat(actual.path("recommended_next_action").asText()).isNotBlank();
    }

    private void assertJsonNullOrEqual(JsonNode actual, JsonNode expected) {
        if (expected.isNull()) {
            assertThat(actual.isNull() || actual.asText().isBlank()).isTrue();
            return;
        }
        assertThat(actual.asText()).isEqualTo(expected.asText());
    }
}
