package com.hackathon.investigator.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueueStormSafetyFilterTest {

    @Test
    void shouldDetectForbiddenRefundPromise() {
        List<String> violations = QueueStormSafetyFilter.findViolations(
                "We will refund you tomorrow.",
                "Verify the transaction details."
        );

        assertThat(violations).anyMatch(v -> v.contains("we will refund"));
    }

    @Test
    void shouldAllowSafeCustomerReply() {
        List<String> violations = QueueStormSafetyFilter.findViolations(
                "Thank you for reaching out. Our team is reviewing the details and any eligible amount will be processed through official channels.",
                "Verify TXN-9101 details with the customer."
        );

        assertThat(violations).isEmpty();
    }
}
