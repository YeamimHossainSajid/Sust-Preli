package com.hackathon.investigator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketAnalysisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthShouldReturnOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")));
    }

    @Test
    void analyzeTicketShouldReturnInvestigationResult() throws Exception {
        String payload = """
                {
                  "ticket_id": "TKT-001",
                  "complaint": "I sent 5000 taka to the wrong number.",
                  "language": "en",
                  "channel": "in_app_chat",
                  "user_type": "customer",
                  "transaction_history": [
                    {
                      "transaction_id": "TXN-9101",
                      "timestamp": "2026-04-14T14:08:22Z",
                      "type": "transfer",
                      "amount": 5000,
                      "counterparty": "+8801719876543",
                      "status": "completed"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analyze-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket_id", is("TKT-001")))
                .andExpect(jsonPath("$.case_type", is("wrong_transfer")))
                .andExpect(jsonPath("$.department", is("dispute_resolution")))
                .andExpect(jsonPath("$.human_review_required", is(true)));
    }

    @Test
    void analyzeTicketShouldAcceptWrappedTestCasePayload() throws Exception {
        String payload = """
                {
                  "id": "SAMPLE-09",
                  "label": "Merchant settlement delay",
                  "input": {
                    "ticket_id": "TKT-009",
                    "complaint": "I am a merchant. My yesterday's sales of 15000 taka have not been settled to my account. Settlement usually happens by 11am next day. Please check.",
                    "language": "en",
                    "channel": "merchant_portal",
                    "user_type": "merchant",
                    "transaction_history": [
                      {
                        "transaction_id": "TXN-9901",
                        "timestamp": "2026-04-13T18:00:00Z",
                        "type": "settlement",
                        "amount": 15000,
                        "counterparty": "MERCHANT-SELF",
                        "status": "pending"
                      }
                    ]
                  },
                  "expected_output": {},
                  "rationale": "Merchant settlement pending"
                }
                """;

        mockMvc.perform(post("/analyze-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket_id", is("TKT-009")))
                .andExpect(jsonPath("$.case_type", is("merchant_settlement_delay")))
                .andExpect(jsonPath("$.department", is("merchant_operations")))
                .andExpect(jsonPath("$.relevant_transaction_id", is("TXN-9901")));
    }

    @Test
    void analyzeTicketShouldHandleAmbiguousTransferMatches() throws Exception {
        String payload = """
                {
                  "ticket_id": "TKT-008",
                  "complaint": "I sent 1000 taka to my brother but he has not received it.",
                  "language": "en",
                  "channel": "in_app_chat",
                  "user_type": "customer",
                  "transaction_history": [
                    {
                      "transaction_id": "TXN-8001",
                      "timestamp": "2026-04-14T10:00:00Z",
                      "type": "transfer",
                      "amount": 1000,
                      "counterparty": "+8801711111111",
                      "status": "completed"
                    },
                    {
                      "transaction_id": "TXN-8002",
                      "timestamp": "2026-04-14T11:00:00Z",
                      "type": "transfer",
                      "amount": 1000,
                      "counterparty": "+8801722222222",
                      "status": "completed"
                    },
                    {
                      "transaction_id": "TXN-8003",
                      "timestamp": "2026-04-14T12:00:00Z",
                      "type": "transfer",
                      "amount": 1000,
                      "counterparty": "+8801733333333",
                      "status": "failed"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/analyze-ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket_id", is("TKT-008")))
                .andExpect(jsonPath("$.relevant_transaction_id").value(nullValue()))
                .andExpect(jsonPath("$.evidence_verdict", is("insufficient_data")))
                .andExpect(jsonPath("$.case_type", is("wrong_transfer")))
                .andExpect(jsonPath("$.human_review_required", is(true)))
                .andExpect(jsonPath("$.confidence", is(0.65)))
                .andExpect(jsonPath("$.reason_codes", hasItems("ambiguous_match", "needs_clarification")))
                .andExpect(jsonPath("$.customer_reply", containsString("brother's number")));
    }
}
