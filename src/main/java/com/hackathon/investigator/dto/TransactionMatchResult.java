package com.hackathon.investigator.dto;

import com.hackathon.investigator.entity.TransactionRecord;

public record TransactionMatchResult(
        TransactionRecord transaction,
        int score
) {
}
