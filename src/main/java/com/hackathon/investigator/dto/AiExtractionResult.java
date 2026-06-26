package com.hackathon.investigator.dto;

import com.hackathon.investigator.enums.RiskLevel;
import com.hackathon.investigator.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public record AiExtractionResult(
        String intent,
        BigDecimal amount,
        TransactionType transactionType,
        RiskLevel riskLevel,
        List<String> keywords,
        String provider
) {
}
