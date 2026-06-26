package com.hackathon.investigator.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends InvestigatorException {

    public AiServiceException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "AI Service Error", message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "AI Service Error", message);
        initCause(cause);
    }
}
