package com.hackathon.investigator.service;

public interface SafetyService {
    String sanitizeCustomerReply(String text);

    String sanitizeAgentText(String text);

    boolean isUnsafe(String text);
}
