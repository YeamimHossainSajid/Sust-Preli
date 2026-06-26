package com.hackathon.investigator.entity;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.enums.TicketJobStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class TicketAnalysisJob {
    private final String jobId;
    private final AnalyzeTicketRequest request;
    private final Instant submittedAt;
    private final CompletableFuture<AnalyzeTicketResponse> completionFuture;

    private volatile TicketJobStatus status;
    private volatile Instant startedAt;
    private volatile Instant completedAt;
    private volatile AnalyzeTicketResponse result;
    private volatile String errorMessage;
    private final AtomicBoolean started;

    public TicketAnalysisJob(
            String jobId,
            AnalyzeTicketRequest request,
            Instant submittedAt,
            CompletableFuture<AnalyzeTicketResponse> completionFuture
    ) {
        this.jobId = jobId;
        this.request = request;
        this.submittedAt = submittedAt;
        this.completionFuture = completionFuture;
        this.status = TicketJobStatus.QUEUED;
        this.started = new AtomicBoolean(false);
    }

    public void markProcessing() {
        if (started.compareAndSet(false, true)) {
            this.status = TicketJobStatus.PROCESSING;
            this.startedAt = Instant.now();
        }
    }

    public void markCompleted(AnalyzeTicketResponse response) {
        this.status = TicketJobStatus.COMPLETED;
        this.result = response;
        this.completedAt = Instant.now();
        this.completionFuture.complete(response);
    }

    public void markFailed(String message) {
        this.status = TicketJobStatus.FAILED;
        this.errorMessage = message;
        this.completedAt = Instant.now();
        this.completionFuture.completeExceptionally(new IllegalStateException(message));
    }
}
