package com.hackathon.investigator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.investigator.enums.TicketJobStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Internal ticket job submission acknowledgement")
public record TicketJobSubmissionResponse(
        @JsonProperty("job_id")
        String jobId,

        TicketJobStatus status,

        @JsonProperty("queue_depth")
        int queueDepth,

        Instant submittedAt
) {
}
