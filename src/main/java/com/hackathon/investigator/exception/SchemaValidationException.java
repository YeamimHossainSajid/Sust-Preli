package com.hackathon.investigator.exception;

import com.hackathon.investigator.dto.ErrorResponse;
import org.springframework.http.HttpStatus;

public class SchemaValidationException extends InvestigatorException {

    private final ErrorResponse errorResponse;

    public SchemaValidationException(ErrorResponse errorResponse) {
        super(HttpStatus.BAD_REQUEST, "Bad Request", errorResponse.message());
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
