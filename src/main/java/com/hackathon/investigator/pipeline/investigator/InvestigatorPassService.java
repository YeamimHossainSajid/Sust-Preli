package com.hackathon.investigator.pipeline.investigator;

import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.pipeline.AnalysisExecutionContext;

public interface InvestigatorPassService {
    InvestigatorPassOutput execute(AnalysisExecutionContext context);
}
