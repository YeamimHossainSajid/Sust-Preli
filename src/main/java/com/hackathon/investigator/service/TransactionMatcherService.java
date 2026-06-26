package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.dto.TransactionMatchAnalysis;
import com.hackathon.investigator.entity.TransactionRecord;

import java.util.List;

public interface TransactionMatcherService {
    TransactionMatchAnalysis analyzeMatches(
            List<TransactionRecord> transactions,
            AiExtractionResult extraction,
            String complaint
    );
}
