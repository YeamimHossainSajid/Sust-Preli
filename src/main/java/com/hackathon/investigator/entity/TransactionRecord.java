package com.hackathon.investigator.entity;

import com.hackathon.investigator.enums.TransactionStatus;
import com.hackathon.investigator.enums.TransactionType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class TransactionRecord {
    String transactionId;
    Instant timestamp;
    TransactionType type;
    BigDecimal amount;
    String counterparty;
    TransactionStatus status;
}
