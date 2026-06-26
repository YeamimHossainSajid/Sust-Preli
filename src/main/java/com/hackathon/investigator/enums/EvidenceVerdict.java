package com.hackathon.investigator.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Evidence consistency verdict")
public enum EvidenceVerdict {
    CONSISTENT("consistent"),
    INCONSISTENT("inconsistent"),
    INSUFFICIENT_DATA("insufficient_data");

    private final String value;

    EvidenceVerdict(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static EvidenceVerdict fromValue(String value) {
        for (EvidenceVerdict verdict : values()) {
            if (verdict.value.equalsIgnoreCase(value)) {
                return verdict;
            }
        }
        throw new IllegalArgumentException("Unknown evidence verdict: " + value);
    }
}
