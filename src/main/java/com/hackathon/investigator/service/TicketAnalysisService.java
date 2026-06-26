package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;

public interface TicketAnalysisService {
    AnalyzeTicketResponse analyze(AnalyzeTicketRequest request);
}
