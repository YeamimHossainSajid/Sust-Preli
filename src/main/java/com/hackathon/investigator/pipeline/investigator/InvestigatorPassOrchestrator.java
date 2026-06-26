package com.hackathon.investigator.pipeline.investigator;

import com.hackathon.investigator.config.AiProperties;
import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.pipeline.AnalysisExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class InvestigatorPassOrchestrator implements InvestigatorPassService {

    private final RuleBasedInvestigatorPassService ruleBasedInvestigatorPassService;
    private final OpenAiInvestigatorPassService openAiInvestigatorPassService;
    private final AiProperties aiProperties;

    @Override
    public InvestigatorPassOutput execute(AnalysisExecutionContext context) {
        if (aiProperties.isEnabled() && openAiInvestigatorPassService.isAvailable()) {
            try {
                return openAiInvestigatorPassService.execute(context);
            } catch (Exception ex) {
                log.warn("Investigator LLM pass failed for {}: {}", context.getRequest().ticketId(), ex.getMessage());
            }
        }
        return ruleBasedInvestigatorPassService.execute(context);
    }
}
