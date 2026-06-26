package com.hackathon.investigator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.investigator.config.QueueProperties;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.pipeline.TicketAnalysisPipeline;
import com.hackathon.investigator.service.TicketAnalysisQueueService;
import com.hackathon.investigator.util.AnalyzeTicketRequestParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@Tag(name = "Investigation", description = "AI-powered complaint investigation endpoints")
public class TicketAnalysisController {

    private final TicketAnalysisQueueService ticketAnalysisQueueService;
    private final TicketAnalysisPipeline ticketAnalysisPipeline;
    private final QueueProperties queueProperties;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/analyze-ticket", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Analyze support ticket",
            description = """
                    Runs the QueueStorm pipeline and returns one structured analysis result.
                    Invalid schema returns HTTP 400; semantic validation failures return HTTP 422;
                    queue saturation returns HTTP 503.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AnalyzeTicketRequest.class),
                    examples = @ExampleObject(name = "Wrapped sample case", value = """
                            {
                              "id": "SAMPLE-01",
                              "label": "Wrong transfer with matching evidence",
                              "input": {
                                "ticket_id": "TKT-001",
                                "complaint": "I sent 5000 taka to a wrong number around 2pm today.",
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
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ticket analyzed successfully",
            content = @Content(schema = @Schema(implementation = AnalyzeTicketResponse.class))
    )
    public AnalyzeTicketResponse analyzeTicket(@RequestBody String rawBody) {
        AnalyzeTicketRequest request = AnalyzeTicketRequestParser.parse(rawBody, objectMapper).normalized();
        ticketAnalysisPipeline.validateIncomingRequest(request);

        var submission = ticketAnalysisQueueService.submit(request);
        return ticketAnalysisQueueService.awaitResult(
                submission.jobId(),
                Duration.ofSeconds(queueProperties.getDefaultWaitSeconds())
        );
    }
}
