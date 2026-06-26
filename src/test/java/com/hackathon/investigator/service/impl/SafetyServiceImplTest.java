package com.hackathon.investigator.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SafetyServiceImplTest {

    private final SafetyServiceImpl safetyService = new SafetyServiceImpl();

    @Test
    void shouldDetectUnsafeOtpRequest() {
        assertThat(safetyService.isUnsafe("Please share your OTP to verify your account")).isTrue();
    }

    @Test
    void shouldAllowSafeCustomerReply() {
        String safe = "We have received your complaint and a specialist will review it shortly.";
        assertThat(safetyService.isUnsafe(safe)).isFalse();
        assertThat(safetyService.sanitizeCustomerReply(safe)).isEqualTo(safe);
    }

    @Test
    void shouldReplaceUnsafeReplyWithFallback() {
        String sanitized = safetyService.sanitizeCustomerReply("Your refund is guaranteed within 1 hour.");
        assertThat(sanitized).contains("received your concern");
    }

    @Test
    void shouldDetectForbiddenRefundPromiseInAgentText() {
        assertThat(safetyService.isUnsafe("We will refund the customer immediately.")).isTrue();
    }
}
