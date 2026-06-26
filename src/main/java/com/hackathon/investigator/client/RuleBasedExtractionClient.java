package com.hackathon.investigator.client;

import com.hackathon.investigator.dto.AiExtractionResult;
import com.hackathon.investigator.enums.RiskLevel;
import com.hackathon.investigator.enums.TransactionType;
import com.hackathon.investigator.util.ComplaintAnalyzer;
import com.hackathon.investigator.util.TextUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class RuleBasedExtractionClient {

    public String getProviderName() {
        return "rule-based";
    }

    public AiExtractionResult extract(String complaint, String language) {
        String normalized = TextUtils.normalize(complaint);
        BigDecimal amount = TextUtils.extractAmount(complaint);
        List<String> keywords = extractKeywords(normalized);
        String intent = detectIntent(normalized, complaint);
        TransactionType type = detectTransactionType(normalized);
        RiskLevel risk = detectRisk(normalized, type);

        return new AiExtractionResult(intent, amount, type, risk, keywords, getProviderName());
    }

    private String detectIntent(String text, String complaint) {
        if (ComplaintAnalyzer.mentionsPhishing(complaint)) {
            return "phishing_or_social_engineering";
        }
        if (ComplaintAnalyzer.mentionsDuplicatePayment(complaint)) {
            return "duplicate_payment";
        }
        if (ComplaintAnalyzer.mentionsPaymentFailed(complaint)) {
            return "payment_failed";
        }
        if (ComplaintAnalyzer.mentionsWrongTransfer(complaint) || ComplaintAnalyzer.mentionsNotReceived(complaint)) {
            return "wrong_transfer";
        }
        if (ComplaintAnalyzer.mentionsMerchantSettlement(complaint)) {
            return "merchant_settlement_delay";
        }
        if (ComplaintAnalyzer.mentionsAgentCashIn(complaint)) {
            return "agent_cash_in_issue";
        }
        if (ComplaintAnalyzer.mentionsRefundRequest(complaint)) {
            return "refund_request";
        }
        return "general_inquiry";
    }

    private TransactionType detectTransactionType(String text) {
        if (containsAny(text, "settlement", "settled", "sales")) {
            return TransactionType.SETTLEMENT;
        }
        if (containsAny(text, "cash in", "cash-in", "cashin", "ক্যাশ ইন")) {
            return TransactionType.CASH_IN;
        }
        if (containsAny(text, "payment", "pay", "recharge", "bill", "electricity")) {
            return TransactionType.PAYMENT;
        }
        if (containsAny(text, "transfer", "send", "sent", "brother", "sister")) {
            return TransactionType.TRANSFER;
        }
        if (containsAny(text, "refund")) {
            return TransactionType.REFUND;
        }
        return TransactionType.TRANSFER;
    }

    private RiskLevel detectRisk(String text, TransactionType type) {
        if (containsAny(text, "otp", "pin", "password", "scam", "phishing", "hack", "called me")) {
            return RiskLevel.CRITICAL;
        }
        if (containsAny(text, "wrong", "duplicate", "failed", "deducted twice")) {
            return RiskLevel.HIGH;
        }
        if (type == TransactionType.SETTLEMENT) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        String[] candidates = {
                "wrong", "transfer", "failed", "refund", "duplicate", "otp", "pin",
                "merchant", "agent", "cash in", "scam", "taka", "payment", "settlement", "brother"
        };
        for (String candidate : candidates) {
            if (text.contains(candidate.toLowerCase(Locale.ROOT))) {
                keywords.add(candidate);
            }
        }
        return keywords;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
