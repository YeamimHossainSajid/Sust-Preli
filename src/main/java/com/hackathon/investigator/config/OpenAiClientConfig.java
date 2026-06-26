package com.hackathon.investigator.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiClientConfig {

    @Bean
    public OpenAIClient openAIClient(OpenAiProperties openAiProperties) {
        return OpenAIOkHttpClient.builder()
                .apiKey(openAiProperties.getApiKey())
                .build();
    }
}
