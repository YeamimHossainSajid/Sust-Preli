package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.exception.SemanticValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SemanticValidationLayerTest {

    private final SemanticValidationLayer layer = new SemanticValidationLayer();

    @Test
    void shouldRejectComplaintShorterThanFiveCharacters() {
        AnalyzeTicketRequest request = new AnalyzeTicketRequest(
                "TKT-001",
                "help",
                "en",
                "in_app_chat",
                "customer",
                null,
                List.of()
        ).normalized();

        assertThatThrownBy(() -> layer.validate(request))
                .isInstanceOf(SemanticValidationException.class)
                .hasMessageContaining("at least 5");
    }

    @Test
    void shouldAcceptValidComplaint() {
        AnalyzeTicketRequest request = new AnalyzeTicketRequest(
                "TKT-001",
                "Something is wrong with my money",
                "en",
                "in_app_chat",
                "customer",
                null,
                List.of()
        ).normalized();

        assertThatCode(() -> layer.validate(request)).doesNotThrowAnyException();
    }
}
