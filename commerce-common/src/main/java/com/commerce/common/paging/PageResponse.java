package com.commerce.common.paging;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single page of results, sorted by id")
public record PageResponse<T>(
        @Schema(description = "Items on this page") List<T> content,
        @Schema(description = "Zero-based page index", example = "0") int page,
        @Schema(description = "Page size actually applied", example = "20") int size,
        @Schema(description = "Total items across all pages", example = "1") long totalElements,
        @Schema(description = "Total number of pages", example = "1") int totalPages) {

    public static <S, T> PageResponse<T> of(Page<S> page, Function<? super S, ? extends T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().<T>map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
