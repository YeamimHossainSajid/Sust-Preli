package com.hackathon.investigator.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketJobStatus {
    QUEUED("queued"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    TicketJobStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
