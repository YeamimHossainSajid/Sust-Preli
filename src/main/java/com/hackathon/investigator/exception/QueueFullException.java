package com.hackathon.investigator.exception;

import org.springframework.http.HttpStatus;

public class QueueFullException extends InvestigatorException {

    public QueueFullException() {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Queue Full",
                "Ticket analysis queue is at capacity. Retry shortly or use horizontal scaling."
        );
    }
}
