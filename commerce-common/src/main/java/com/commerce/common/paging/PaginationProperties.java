package com.commerce.common.paging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.pagination")
public record PaginationProperties(Integer defaultSize, Integer maxSize) {

    public PaginationProperties {
        defaultSize = defaultSize == null ? 20 : defaultSize;
        maxSize = maxSize == null ? 100 : maxSize;
    }
}
