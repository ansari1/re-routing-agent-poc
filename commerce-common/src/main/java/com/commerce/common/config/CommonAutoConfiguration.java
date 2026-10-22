package com.commerce.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.commerce.common.error.GlobalExceptionHandler;
import com.commerce.common.openapi.OpenApiDefaults;
import com.commerce.common.paging.PageRequestFactory;
import com.commerce.common.paging.PaginationProperties;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/** Registers the shared error handling, paging, cache and OpenAPI beans in every service. */
@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties({CacheProperties.class, PaginationProperties.class})
@Import(GlobalExceptionHandler.class)
@PropertySource("classpath:commerce-openapi.properties")
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    PageRequestFactory pageRequestFactory(PaginationProperties properties) {
        return new PageRequestFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    CacheManager cacheManager(CacheProperties properties) {
        var manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(properties.ttl())
                .maximumSize(properties.maximumSize()));
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean
    OpenAPI openApi(
            @Value("${spring.application.name}") String name,
            @Value("${app.api.version:1.0.0}") String version,
            @Value("${app.api.description:Commerce platform microservice}") String description) {
        return new OpenAPI().info(new Info().title(name).version(version).description(description));
    }

    @Bean
    @ConditionalOnMissingBean
    OpenApiDefaults openApiDefaults(PaginationProperties properties) {
        return new OpenApiDefaults(properties);
    }
}
