package io.github.ilkinnnnn.urlshortener.model.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse <T>(
        List<T> elements,
        int page,
        int pageSize,
        long totalElements,
        int totalPages
) {
    public PageResponse (Page<T> page) {
        this(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
