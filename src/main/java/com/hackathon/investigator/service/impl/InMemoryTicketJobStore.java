package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.config.QueueProperties;
import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.entity.TicketAnalysisJob;
import com.hackathon.investigator.enums.TicketJobStatus;
import com.hackathon.investigator.exception.InvestigatorException;
import com.hackathon.investigator.exception.JobNotFoundException;
import com.hackathon.investigator.service.TicketJobStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class InMemoryTicketJobStore implements TicketJobStore {

    private final Map<String, TicketAnalysisJob> jobs = new ConcurrentHashMap<>();
    private final QueueProperties queueProperties;

    public InMemoryTicketJobStore(QueueProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    @Override
    public TicketAnalysisJob create(AnalyzeTicketRequest request) {
        if (jobs.size() >= queueProperties.getMaxStoredJobs()) {
            cleanupExpiredJobs(Duration.ofMinutes(queueProperties.getJobTtlMinutes()));
        }

        String jobId = "job-" + UUID.randomUUID();
        TicketAnalysisJob job = new TicketAnalysisJob(
                jobId,
                request.normalized(),
                Instant.now(),
                new CompletableFuture<>()
        );
        jobs.put(jobId, job);
        return job;
    }

    @Override
    public TicketAnalysisJob getRequired(String jobId) {
        return find(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
    }

    @Override
    public Optional<TicketAnalysisJob> find(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    @Override
    public AnalyzeTicketResponse awaitResult(String jobId, Duration timeout) {
        TicketAnalysisJob job = getRequired(jobId);
        try {
            return job.getCompletionFuture().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            throw new InvestigatorException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Processing Timeout",
                    "Ticket analysis job " + jobId + " did not complete within " + timeout.getSeconds() + " seconds. "
                            + "Poll GET /analyze-ticket/jobs/" + jobId
            );
        } catch (Exception ex) {
            if (job.getErrorMessage() != null) {
                throw new InvestigatorException(HttpStatus.INTERNAL_SERVER_ERROR, "Job Failed", job.getErrorMessage());
            }
            throw new InvestigatorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Job Failed",
                    "Ticket analysis job failed: " + ex.getMessage()
            );
        }
    }

    @Override
    public void cleanupExpiredJobs(Duration ttl) {
        Instant cutoff = Instant.now().minus(ttl);
        Iterator<Map.Entry<String, TicketAnalysisJob>> iterator = jobs.entrySet().iterator();
        while (iterator.hasNext()) {
            TicketAnalysisJob job = iterator.next().getValue();
            Instant reference = job.getCompletedAt() != null ? job.getCompletedAt() : job.getSubmittedAt();
            if (reference.isBefore(cutoff)
                    && (job.getStatus() == TicketJobStatus.COMPLETED || job.getStatus() == TicketJobStatus.FAILED)) {
                iterator.remove();
            }
        }
        log.debug("Job store size after cleanup: {}", jobs.size());
    }

    @Override
    public long countByStatus(TicketJobStatus status) {
        return jobs.values().stream().filter(job -> job.getStatus() == status).count();
    }

    @Override
    public int size() {
        return jobs.size();
    }
}
