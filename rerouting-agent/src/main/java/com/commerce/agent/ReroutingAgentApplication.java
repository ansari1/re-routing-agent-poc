package com.commerce.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReroutingAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReroutingAgentApplication.class, args);
    }
}
