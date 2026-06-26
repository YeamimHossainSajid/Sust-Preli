package com.hackathon.investigator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.exception.SchemaValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalyzeTicketRequestParserFriendlyErrorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnFriendlyMessageForMalformedStructure() {
        assertThatThrownBy(() -> AnalyzeTicketRequestParser.parse(
                "\"ticket_id\"",
                objectMapper
        ))
                .isInstanceOf(SchemaValidationException.class)
                .satisfies(ex -> {
                    SchemaValidationException schemaEx = (SchemaValidationException) ex;
                    assertThat(schemaEx.getErrorResponse().message())
                            .contains("Invalid JSON structure")
                            .doesNotContain("AnalyzeTicketRequest")
                            .doesNotContain("Creator");
                    assertThat(schemaEx.getErrorResponse().fieldErrors()).isNotNull();
                });
    }

    @Test
    void shouldReturnFriendlyMessageForIncompleteJson() {
        assertThatThrownBy(() -> AnalyzeTicketRequestParser.parse(
                "{\"ticket_id\":\"TKT-001\"",
                objectMapper
        ))
                .isInstanceOf(SchemaValidationException.class)
                .satisfies(ex -> {
                    SchemaValidationException schemaEx = (SchemaValidationException) ex;
                    assertThat(schemaEx.getErrorResponse().message())
                            .contains("Incomplete JSON");
                });
    }

    @Test
    void shouldStillParseValidWrappedRequest() throws Exception {
        AnalyzeTicketRequest request = AnalyzeTicketRequestParser.parse(
                """
                {
                  "request": {
                    "ticket_id": "TKT-001",
                    "complaint": "I sent money to the wrong number.",
                    "language": "en",
                    "channel": "in_app_chat",
                    "user_type": "customer",
                    "transaction_history": []
                  }
                }
                """,
                objectMapper
        );

        assertThat(request.ticketId()).isEqualTo("TKT-001");
    }
}
