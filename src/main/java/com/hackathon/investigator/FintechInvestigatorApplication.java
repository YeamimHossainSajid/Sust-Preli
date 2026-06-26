package com.hackathon.investigator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FintechInvestigatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintechInvestigatorApplication.class, args);
    }
}
