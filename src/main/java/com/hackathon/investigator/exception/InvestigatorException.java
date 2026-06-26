package com.hackathon.investigator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvestigatorException extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public InvestigatorException(HttpStatus status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }
}
