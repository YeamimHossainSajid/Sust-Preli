package com.hackathon.investigator.config;

import com.hackathon.investigator.service.TicketJobStore;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TicketJobCleanupScheduler {

    private final TicketJobStore ticketJobStore;
    private final QueueProperties queueProperties;

    @Scheduled(fixedDelayString = "${investigator.queue.cleanup-interval-ms:300000}")
    public void cleanupExpiredJobs() {
        ticketJobStore.cleanupExpiredJobs(Duration.ofMinutes(queueProperties.getJobTtlMinutes()));
    }
}
