package io.github.ilkinnnnn.urlshortener.model.response;

import java.time.LocalDateTime;

public record UrlResponse(
        Long id,
        String originalUrl,
        String shortCode,
        Long clickCount,
        LocalDateTime createdAt,
        LocalDateTime lastAccessedAt
) {
}
