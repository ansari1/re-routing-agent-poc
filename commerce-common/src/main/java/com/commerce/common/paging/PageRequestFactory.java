package com.commerce.common.paging;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Single place where client paging input is bounded. */
public class PageRequestFactory {

    private final PaginationProperties properties;

    public PageRequestFactory(PaginationProperties properties) {
        this.properties = properties;
    }

    public Pageable create(int page, Integer size, Sort sort) {
        int safePage = Math.max(page, 0);
        int requested = size == null ? properties.defaultSize() : size;
        int safeSize = Math.min(Math.max(requested, 1), properties.maxSize());
        return PageRequest.of(safePage, safeSize, sort);
    }

    public Pageable create(int page, Integer size) {
        return create(page, size, Sort.by("id"));
    }
}
