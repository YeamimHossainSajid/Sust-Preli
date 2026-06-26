package com.hackathon.investigator.pipeline.investigator;

import com.hackathon.investigator.dto.InvestigatorPassOutput;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.pipeline.AnalysisExecutionContext;
import com.hackathon.investigator.service.CaseClassificationService;
import com.hackathon.investigator.service.EvidenceService;
import com.hackathon.investigator.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleBasedInvestigatorPassService implements InvestigatorPassService {

    private final EvidenceService evidenceService;
    private final CaseClassificationService caseClassificationService;
    private final RoutingService routingService;

    @Override
    public InvestigatorPassOutput execute(AnalysisExecutionContext context) {
        EvidenceVerdict evidenceVerdict = resolveEvidenceVerdict(context);
        CaseType caseType = caseClassificationService.classify(
                context.getRequest().complaint(),
                context.getExtraction()
        );
        Department department = routingService.route(caseType, evidenceVerdict);

        context.applyInvestigatorPass(evidenceVerdict, caseType, department);

        return new InvestigatorPassOutput(
                context.relevantTransactionId(),
                evidenceVerdict.getValue(),
                caseType.getValue(),
                department.getValue()
        );
    }

    private EvidenceVerdict resolveEvidenceVerdict(AnalysisExecutionContext context) {
        if (context.isAmbiguousMatch() || context.isVagueComplaint()) {
            return EvidenceVerdict.INSUFFICIENT_DATA;
        }

        return evidenceService.evaluate(
                context.getRequest().complaint(),
                context.getExtraction(),
                context.getMatchedTransaction(),
                context.isEstablishedRecipientPattern(),
                context.isDuplicatePaymentScenario(),
                context.getTransactions()
        );
    }
}
