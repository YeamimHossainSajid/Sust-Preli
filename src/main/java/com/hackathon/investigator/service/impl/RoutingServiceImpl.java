package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.Department;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.service.RoutingService;
import org.springframework.stereotype.Service;

@Service
public class RoutingServiceImpl implements RoutingService {

    @Override
    public Department route(CaseType caseType, EvidenceVerdict evidenceVerdict) {
        if (evidenceVerdict == EvidenceVerdict.INCONSISTENT && isFinancialCase(caseType)) {
            return Department.DISPUTE_RESOLUTION;
        }

        if (caseType == CaseType.REFUND_REQUEST && evidenceVerdict == EvidenceVerdict.INCONSISTENT) {
            return Department.DISPUTE_RESOLUTION;
        }

        return switch (caseType) {
            case WRONG_TRANSFER -> Department.DISPUTE_RESOLUTION;
            case PAYMENT_FAILED, DUPLICATE_PAYMENT -> Department.PAYMENTS_OPS;
            case MERCHANT_SETTLEMENT_DELAY -> Department.MERCHANT_OPERATIONS;
            case AGENT_CASH_IN_ISSUE -> Department.AGENT_OPERATIONS;
            case PHISHING_OR_SOCIAL_ENGINEERING -> Department.FRAUD_RISK;
            case REFUND_REQUEST, OTHER -> Department.CUSTOMER_SUPPORT;
        };
    }

    private boolean isFinancialCase(CaseType caseType) {
        return switch (caseType) {
            case WRONG_TRANSFER, PAYMENT_FAILED, REFUND_REQUEST, DUPLICATE_PAYMENT,
                 MERCHANT_SETTLEMENT_DELAY, AGENT_CASH_IN_ISSUE -> true;
            default -> false;
        };
    }
}
