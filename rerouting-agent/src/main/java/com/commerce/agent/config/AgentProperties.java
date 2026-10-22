package com.commerce.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.agent")
public record AgentProperties(Integer maxRerouteAttempts, Gemini gemini) {

    public AgentProperties {
        maxRerouteAttempts = maxRerouteAttempts == null ? 3 : maxRerouteAttempts;
        gemini = gemini == null ? new Gemini(false, null, null, null, null) : gemini;
    }

    /** Vertex AI Gemini settings; credentials come from Application Default Credentials, never from config. */
    public record Gemini(Boolean enabled, String project, String location, String model, Float temperature) {

        public Gemini {
            enabled = enabled != null && enabled;
            location = location == null ? "us-central1" : location;
            model = model == null ? "gemini-2.0-flash" : model;
            temperature = temperature == null ? 0f : temperature;
        }
    }
}
