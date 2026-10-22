package com.commerce.agent.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.commerce.agent.routing.GeminiRouteAdvisor;
import com.commerce.agent.routing.PriorityRouteAdvisor;
import com.commerce.agent.routing.RouteAdvisor;
import com.commerce.agent.routing.RoutingAssistant;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

@Configuration
public class RoutingConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.agent.gemini", name = "enabled", havingValue = "true")
    ChatModel geminiChatModel(AgentProperties properties) {
        var gemini = properties.gemini();
        return VertexAiGeminiChatModel.builder()
                .project(gemini.project())
                .location(gemini.location())
                .modelName(gemini.model())
                .temperature(gemini.temperature())
                .build();
    }

    @Bean
    RouteAdvisor routeAdvisor(ObjectProvider<ChatModel> chatModel) {
        var rules = new PriorityRouteAdvisor();
        return chatModel.stream().findFirst()
                .<RouteAdvisor>map(model -> new GeminiRouteAdvisor(AiServices.create(RoutingAssistant.class, model), rules))
                .orElse(rules);
    }
}
