package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AiExtractionResult;

public interface AiExtractionService {
    AiExtractionResult extract(String complaint, String language);
}
