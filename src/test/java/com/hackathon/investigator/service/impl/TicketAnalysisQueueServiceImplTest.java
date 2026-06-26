package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.TransactionHistoryDto;
import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.enums.TransactionType;
import com.hackathon.investigator.enums.TicketJobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TicketAnalysisQueueServiceImplTest {

    @Autowired
    private TicketAnalysisQueueServiceImpl queueService;

    @Test
    void shouldProcessQueuedTicketSynchronously() {
        AnalyzeTicketRequest request = new AnalyzeTicketRequest(
                "TKT-Q-001",
                "I sent 5000 taka to the wrong number.",
                "en",
                "in_app_chat",
                "customer",
                null,
                List.of(new TransactionHistoryDto(
                        "TXN-9101",
                        Instant.parse("2026-04-14T14:08:22Z"),
                        TransactionType.TRANSFER,
                        new BigDecimal("5000"),
                        "+8801719876543",
                        TransactionStatus.COMPLETED
                ))
        ).normalized();

        var submission = queueService.submit(request);
        assertThat(submission.status()).isEqualTo(TicketJobStatus.QUEUED);

        var response = queueService.awaitResult(submission.jobId(), Duration.ofSeconds(30));
        assertThat(response.ticketId()).isEqualTo("TKT-Q-001");
        assertThat(response.caseType().getValue()).isEqualTo("wrong_transfer");
        assertThat(response.humanReviewRequired()).isTrue();
    }
}
