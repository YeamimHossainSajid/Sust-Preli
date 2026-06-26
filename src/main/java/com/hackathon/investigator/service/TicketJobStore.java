package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.entity.TicketAnalysisJob;
import com.hackathon.investigator.exception.JobNotFoundException;

import java.time.Duration;
import java.util.Optional;

public interface TicketJobStore {
    TicketAnalysisJob create(AnalyzeTicketRequest request);

    TicketAnalysisJob getRequired(String jobId);

    Optional<TicketAnalysisJob> find(String jobId);

    AnalyzeTicketResponse awaitResult(String jobId, Duration timeout);

    void cleanupExpiredJobs(Duration ttl);

    long countByStatus(com.hackathon.investigator.enums.TicketJobStatus status);

    int size();
}
