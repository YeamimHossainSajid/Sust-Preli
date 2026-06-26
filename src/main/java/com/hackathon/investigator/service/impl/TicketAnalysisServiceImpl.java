package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.AnalyzeTicketResponse;
import com.hackathon.investigator.pipeline.TicketAnalysisPipeline;
import com.hackathon.investigator.service.TicketAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketAnalysisServiceImpl implements TicketAnalysisService {

    private final TicketAnalysisPipeline ticketAnalysisPipeline;

    @Override
    public AnalyzeTicketResponse analyze(AnalyzeTicketRequest rawRequest) {
        return ticketAnalysisPipeline.process(rawRequest);
    }
}
