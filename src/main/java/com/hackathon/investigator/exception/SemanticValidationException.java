package com.hackathon.investigator.exception;

import org.springframework.http.HttpStatus;

public class SemanticValidationException extends InvestigatorException {

    public SemanticValidationException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", message);
    }
}
