package com.hackathon.investigator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "investigator.queue")
public class QueueProperties {
    private int capacity = 50000;
    private int workers = 256;
    private int jobTtlMinutes = 60;
    private int defaultWaitSeconds = 120;
    private int enqueueTimeoutMs = 100;
    private int maxStoredJobs = 100000;
}
