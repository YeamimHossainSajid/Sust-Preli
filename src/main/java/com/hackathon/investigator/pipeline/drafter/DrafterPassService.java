package com.hackathon.investigator.pipeline.drafter;

import com.hackathon.investigator.dto.DrafterPassOutput;
import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.pipeline.AnalysisExecutionContext;

public interface DrafterPassService {
    DrafterPassOutput execute(AnalysisExecutionContext context, InvestigatorPassOutput investigatorPass);
}
