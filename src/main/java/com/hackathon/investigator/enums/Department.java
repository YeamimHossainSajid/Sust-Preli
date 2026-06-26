package com.hackathon.investigator.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Routing department for case handling")
public enum Department {
    CUSTOMER_SUPPORT("customer_support"),
    DISPUTE_RESOLUTION("dispute_resolution"),
    PAYMENTS_OPS("payments_ops"),
    MERCHANT_OPERATIONS("merchant_operations"),
    AGENT_OPERATIONS("agent_operations"),
    FRAUD_RISK("fraud_risk");

    private final String value;

    Department(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Department fromValue(String value) {
        for (Department department : values()) {
            if (department.value.equalsIgnoreCase(value)) {
                return department;
            }
        }
        throw new IllegalArgumentException("Unknown department: " + value);
    }
}
