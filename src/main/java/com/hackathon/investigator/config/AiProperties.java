package com.hackathon.investigator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "investigator.ai")
public class AiProperties {
    private boolean enabled = true;
    private double temperature = 0.1;
    private int maxTokens = 1200;
    private double topP = 0.9;
    private int pipelineTimeoutSeconds = 90;
    private String investigatorModel = "gpt-3.5-turbo";
    private String drafterModel = "gpt-4.1-mini";
}
