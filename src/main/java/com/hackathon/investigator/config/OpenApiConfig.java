package com.hackathon.investigator.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fintechInvestigatorOpenApi(
            @Value("${spring.application.name:fintech-investigator}") String appName
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("Fintech Investigator API")
                        .description("""
                                AI-powered fintech complaint investigation system for hackathon demos.
                                Supports English, Bangla, and Banglish complaint analysis with transaction matching,
                                evidence evaluation, case classification, routing, and safe response generation.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Hackathon Team").email("team@example.com"))
                        .license(new License().name("MIT")))
                .addServersItem(new Server().url("/").description(appName))
                .components(new Components()
                        .addResponses("BadRequest", new ApiResponse()
                                .description("Invalid request payload")
                                .content(new Content().addMediaType(
                                        "application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                                ))));
    }
}
