package com.hackathon.investigator.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction processing status")
public enum TransactionStatus {
    COMPLETED("completed"),
    FAILED("failed"),
    PENDING("pending"),
    REVERSED("reversed");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TransactionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TransactionStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
