package com.hackathon.investigator.pipeline.drafter;

import com.hackathon.investigator.config.AiProperties;
import com.hackathon.investigator.dto.DrafterPassOutput;
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
public class DrafterPassOrchestrator implements DrafterPassService {

    private final RuleBasedDrafterPassService ruleBasedDrafterPassService;
    private final OpenAiDrafterPassService openAiDrafterPassService;
    private final AiProperties aiProperties;

    @Override
    public DrafterPassOutput execute(AnalysisExecutionContext context, InvestigatorPassOutput investigatorPass) {
        if (aiProperties.isEnabled() && openAiDrafterPassService.isAvailable()) {
            try {
                return openAiDrafterPassService.execute(context, investigatorPass);
            } catch (Exception ex) {
                log.warn("Drafter LLM pass failed for {}: {}", context.getRequest().ticketId(), ex.getMessage());
            }
        }
        return ruleBasedDrafterPassService.execute(context, investigatorPass);
    }
}
