package com.hackathon.investigator.exception;

import org.springframework.http.HttpStatus;

public class JobNotFoundException extends InvestigatorException {

    public JobNotFoundException(String jobId) {
        super(HttpStatus.NOT_FOUND, "Job Not Found", "No ticket analysis job found for id: " + jobId);
    }
}
