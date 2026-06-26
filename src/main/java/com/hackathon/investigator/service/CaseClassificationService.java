package com.hackathon.investigator.service;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.enums.CaseType;

public interface CaseClassificationService {
    CaseType classify(String complaint, AiExtractionResult extraction);
}
