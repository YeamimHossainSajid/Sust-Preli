package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.client.RuleBasedExtractionClient;
import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.service.AiExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiExtractionServiceImpl implements AiExtractionService {

    private final RuleBasedExtractionClient ruleBasedExtractionClient;

    @Override
    public AiExtractionResult extract(String complaint, String language) {
        log.info("Extracting complaint signals using provider={}", ruleBasedExtractionClient.getProviderName());
        return ruleBasedExtractionClient.extract(complaint, language);
    }
}
