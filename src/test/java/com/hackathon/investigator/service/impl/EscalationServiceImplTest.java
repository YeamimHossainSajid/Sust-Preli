package com.hackathon.investigator.service.impl;

import com.hackathon.investigator.enums.CaseType;
import com.hackathon.investigator.enums.EvidenceVerdict;
import com.hackathon.investigator.enums.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EscalationServiceImplTest {

    private final EscalationServiceImpl service = new EscalationServiceImpl();

    @Test
    void shouldRequireHumanReviewForWrongTransfer() {
        assertThat(service.requiresHumanReview(
                CaseType.WRONG_TRANSFER,
                EvidenceVerdict.CONSISTENT,
                Severity.HIGH,
                false,
                false,
                false,
                0.9
        )).isTrue();
    }

    @Test
    void shouldRequireHumanReviewForAmbiguousWrongTransfer() {
        assertThat(service.requiresHumanReview(
                CaseType.WRONG_TRANSFER,
                EvidenceVerdict.INSUFFICIENT_DATA,
                Severity.MEDIUM,
                false,
                false,
                false,
                0.65
        )).isTrue();
    }

    @Test
    void shouldNotRequireHumanReviewForOtherInsufficientData() {
        assertThat(service.requiresHumanReview(
                CaseType.OTHER,
                EvidenceVerdict.INSUFFICIENT_DATA,
                Severity.LOW,
                false,
                false,
                false,
                0.6
        )).isFalse();
    }

    @Test
    void shouldRequireHumanReviewForCriticalSeverity() {
        assertThat(service.requiresHumanReview(
                CaseType.OTHER,
                EvidenceVerdict.CONSISTENT,
                Severity.CRITICAL,
                false,
                false,
                false,
                0.9
        )).isTrue();
    }

    @Test
    void shouldRequireHumanReviewWhenCampaignContextPresent() {
        assertThat(service.requiresHumanReview(
                CaseType.REFUND_REQUEST,
                EvidenceVerdict.CONSISTENT,
                Severity.LOW,
                true,
                false,
                false,
                0.85
        )).isTrue();
    }

    @Test
    void shouldRequireHumanReviewForEmptyTransactionHistory() {
        assertThat(service.requiresHumanReview(
                CaseType.PHISHING_OR_SOCIAL_ENGINEERING,
                EvidenceVerdict.INSUFFICIENT_DATA,
                Severity.CRITICAL,
                false,
                true,
                false,
                0.95
        )).isTrue();
    }
}
