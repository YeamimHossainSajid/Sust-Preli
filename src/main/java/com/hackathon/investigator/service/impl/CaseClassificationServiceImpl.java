package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.RiskLevel;
import com.hackathon.investigator.service.CaseClassificationService;
import com.hackathon.investigator.util.ComplaintAnalyzer;
import com.hackathon.investigator.util.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CaseClassificationServiceImpl implements CaseClassificationService {

    @Override
    public CaseType classify(String complaint, AiExtractionResult extraction) {
        if (ComplaintAnalyzer.isAdversarialComplaint(complaint)) {
            return CaseType.OTHER;
        }

        if (ComplaintAnalyzer.isVagueComplaint(complaint)) {
            return CaseType.OTHER;
        }

        if (ComplaintAnalyzer.mentionsPhishing(complaint)) {
            return CaseType.PHISHING_OR_SOCIAL_ENGINEERING;
        }

        String intent = extraction.intent() == null ? "" : extraction.intent().toLowerCase();
        CaseType fromIntent = mapIntent(intent);
        if (fromIntent != CaseType.OTHER) {
            return fromIntent;
        }

        if (ComplaintAnalyzer.mentionsDuplicatePayment(complaint)) {
            return CaseType.DUPLICATE_PAYMENT;
        }
        if (ComplaintAnalyzer.mentionsPaymentFailed(complaint)) {
            return CaseType.PAYMENT_FAILED;
        }
        if (ComplaintAnalyzer.mentionsWrongTransfer(complaint) || ComplaintAnalyzer.mentionsNotReceived(complaint)) {
            return CaseType.WRONG_TRANSFER;
        }
        if (ComplaintAnalyzer.mentionsMerchantSettlement(complaint)) {
            return CaseType.MERCHANT_SETTLEMENT_DELAY;
        }
        if (ComplaintAnalyzer.mentionsAgentCashIn(complaint)) {
            return CaseType.AGENT_CASH_IN_ISSUE;
        }
        if (ComplaintAnalyzer.mentionsRefundRequest(complaint)) {
            return CaseType.REFUND_REQUEST;
        }

        if (extraction.riskLevel() == RiskLevel.CRITICAL) {
            return CaseType.PHISHING_OR_SOCIAL_ENGINEERING;
        }

        return CaseType.OTHER;
    }

    private CaseType mapIntent(String intent) {
        return switch (intent) {
            case "wrong_transfer" -> CaseType.WRONG_TRANSFER;
            case "payment_failed" -> CaseType.PAYMENT_FAILED;
            case "refund_request" -> CaseType.REFUND_REQUEST;
            case "duplicate_payment" -> CaseType.DUPLICATE_PAYMENT;
            case "merchant_settlement_delay" -> CaseType.MERCHANT_SETTLEMENT_DELAY;
            case "agent_cash_in_issue" -> CaseType.AGENT_CASH_IN_ISSUE;
            case "phishing_or_social_engineering" -> CaseType.PHISHING_OR_SOCIAL_ENGINEERING;
            default -> CaseType.OTHER;
        };
    }
}
