package com.hackathon.investigator.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Financial transaction type")
public enum TransactionType {
    TRANSFER("transfer"),
    PAYMENT("payment"),
    CASH_IN("cash_in"),
    CASH_OUT("cash_out"),
    SETTLEMENT("settlement"),
    REFUND("refund");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TransactionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TransactionType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
