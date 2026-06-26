package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.config.AiProperties;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.dto.DrafterPassOutput;
import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.exception.InvestigatorException;
import com.hackathon.investigator.pipeline.drafter.DrafterPassService;
import com.hackathon.investigator.pipeline.investigator.InvestigatorPassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAnalysisPipeline {

    private final SchemaValidationLayer schemaValidationLayer;
    private final SemanticValidationLayer semanticValidationLayer;
    private final PreFlightSafetyCheck preFlightSafetyCheck;
    private final AnalysisContextFactory analysisContextFactory;
    private final InvestigatorPassService investigatorPassService;
    private final DrafterPassService drafterPassService;
    private final PostFlightSafetyGuardrail postFlightSafetyGuardrail;
    private final FinalValidationLayer finalValidationLayer;
    private final AiProperties aiProperties;

    public void validateIncomingRequest(AnalyzeTicketRequest request) {
        schemaValidationLayer.validate(request);
        semanticValidationLayer.validate(request);
    }

    public AnalyzeTicketResponse process(AnalyzeTicketRequest rawRequest) {
        AnalyzeTicketRequest request = rawRequest.normalized();
        log.info("Starting analysis pipeline for ticket {}", request.ticketId());

        schemaValidationLayer.validate(request);
        semanticValidationLayer.validate(request);

        boolean flagged = preFlightSafetyCheck.isFlagged(request.complaint());
        AnalysisExecutionContext context = analysisContextFactory.create(request, flagged);

        try {
            InvestigatorPassOutput investigatorPass = CompletableFuture
                    .supplyAsync(() -> investigatorPassService.execute(context))
                    .orTimeout(passTimeoutSeconds(), TimeUnit.SECONDS)
                    .join();

            DrafterPassOutput drafterPass = CompletableFuture
                    .supplyAsync(() -> drafterPassService.execute(context, investigatorPass))
                    .orTimeout(passTimeoutSeconds(), TimeUnit.SECONDS)
                    .join();

            DrafterPassOutput sanitizedPass = postFlightSafetyGuardrail.sanitize(drafterPass);

            AnalyzeTicketResponse response = finalValidationLayer.validateAndConstruct(
                    request,
                    investigatorPass,
                    sanitizedPass,
                    flagged
            );

            log.info(
                    "Pipeline completed for {}: flagged={}, caseType={}, evidence={}",
                    request.ticketId(),
                    flagged,
                    response.caseType(),
                    response.evidenceVerdict()
            );

            return response;
        } catch (Exception ex) {
            if (ex.getCause() instanceof TimeoutException) {
                throw new InvestigatorException(
                        HttpStatus.GATEWAY_TIMEOUT,
                        "Gateway Timeout",
                        "LLM pipeline exceeded the configured time limit"
                );
            }
            if (ex instanceof InvestigatorException investigatorException) {
                throw investigatorException;
            }
            log.error("Unexpected pipeline failure for ticket {}", request.ticketId(), ex);
            throw new InvestigatorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    "An unexpected error occurred"
            );
        }
    }

    private long passTimeoutSeconds() {
        int totalSeconds = aiProperties.getPipelineTimeoutSeconds();
        return Math.max(5, totalSeconds / 2L);
    }
}
