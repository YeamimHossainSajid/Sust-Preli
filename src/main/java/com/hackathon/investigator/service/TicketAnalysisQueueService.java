package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.dto.TicketJobSubmissionResponse;

import java.time.Duration;

public interface TicketAnalysisQueueService {

    TicketJobSubmissionResponse submit(AnalyzeTicketRequest request);

    AnalyzeTicketResponse awaitResult(String jobId, Duration timeout);
}
