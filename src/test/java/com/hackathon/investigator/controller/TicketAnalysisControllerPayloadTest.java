package com.hackathon.investigator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketAnalysisControllerPayloadTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAcceptWrappedSampleCaseWithCampaignContext() throws Exception {
        String payload = """
                {
                  "id": "SAMPLE-01",
                  "label": "Wrong transfer with matching evidence",
                  "input": {
                    "ticket_id": "TKT-001",
                    "complaint": "I sent 5000 taka to a wrong number around 2pm today. The number was supposed to be 01712345678 but I think I typed it wrong. The person isn't responding to my call. Please help me get my money back.",
                    "language": "en",
                    "channel": "in_app_chat",
                    "user_type": "customer",
                    "campaign_context": "boishakh_bonanza_day_1",
                    "transaction_history": [
                      {
                        "transaction_id": "TXN-9101",
                        "timestamp": "2026-04-14T14:08:22Z",
                        "type": "transfer",
                        "amount": 5000,
                        "counterparty": "+8801719876543",
                        "status": "completed"
                      },
                      {
                        "transaction_id": "TXN-9087",
                        "timestamp": "2026-04-13T18:12:00Z",
                        "type": "cash_in",
                        "amount": 10000,
                        "counterparty": "AGENT-512",
                        "status": "completed"
                      }
                    ]
                  },
                  "expected_output": {
                    "ticket_id": "TKT-001",
                    "relevant_transaction_id": "TXN-9101",
                    "evidence_verdict": "consistent",
                    "case_type": "wrong_transfer",
                    "severity": "high",
                    "department": "dispute_resolution",
                    "human_review_required": true,
                    "confidence": 0.9,
                    "reason_codes": ["wrong_transfer", "transaction_match", "dispute_initiated"]
                  }
                }
                """;

        mockMvc.perform(post("/analyze-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket_id").value("TKT-001"))
                .andExpect(jsonPath("$.relevant_transaction_id").value("TXN-9101"))
                .andExpect(jsonPath("$.evidence_verdict").value("consistent"))
                .andExpect(jsonPath("$.case_type").value("wrong_transfer"))
                .andExpect(jsonPath("$.severity").value("high"))
                .andExpect(jsonPath("$.department").value("dispute_resolution"))
                .andExpect(jsonPath("$.human_review_required").value(true))
                .andExpect(jsonPath("$.confidence").value(0.9))
                .andExpect(jsonPath("$.reason_codes", containsInAnyOrder(
                        "wrong_transfer", "transaction_match", "dispute_initiated"
                )))
                .andExpect(jsonPath("$.agent_summary").isNotEmpty())
                .andExpect(jsonPath("$.customer_reply").value(
                        "We have noted your concern about transaction TXN-9101. Please do not share your PIN or OTP with anyone. Our dispute team will review the case and contact you through official support channels."
                ));
    }

    @Test
    void shouldAcceptJudgeStyleRequestWrapper() throws Exception {
        String payload = """
                {
                  "_id": "PH-005",
                  "_label": "phishing_or_social_engineering — CAMPAIGN EXPLOITATION",
                  "_rationale": "Fraudsters often impersonate the platform during campaigns.",
                  "request": {
                    "ticket_id": "TKT-PH-005",
                    "complaint": "I got a message saying I qualified for 500 taka cashback from the Boishakh campaign and need to click a link and enter my bKash PIN to claim it. I clicked the link but did not enter my PIN yet. The site looked almost like bKash.",
                    "language": "en",
                    "channel": "in_app_chat",
                    "user_type": "customer",
                    "campaign_context": "boishakh_bonanza_day_1",
                    "transaction_history": []
                  }
                }
                """;

        mockMvc.perform(post("/analyze-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket_id").value("TKT-PH-005"))
                .andExpect(jsonPath("$.case_type").value("phishing_or_social_engineering"))
                .andExpect(jsonPath("$.human_review_required").value(true));
    }
}
