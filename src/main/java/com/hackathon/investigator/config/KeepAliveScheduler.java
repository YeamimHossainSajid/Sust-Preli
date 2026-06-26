package com.hackathon.investigator.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeepAliveScheduler {

    private final RestTemplate restTemplate;

    @Value("${investigator.keep-alive.url:https://sust-preli-b8l9.onrender.com/health}")
    private String healthCheckUrl;

    @Scheduled(fixedRateString = "${investigator.keep-alive.interval-ms:300000}")
    public void pingBackend() {
        try {
            restTemplate.getForObject(healthCheckUrl, String.class);
            log.info("Pinged Render backend successfully: {}", healthCheckUrl);
        } catch (Exception ex) {
            log.error("Failed to ping Render backend at {}: {}", healthCheckUrl, ex.getMessage());
        }
    }
}
