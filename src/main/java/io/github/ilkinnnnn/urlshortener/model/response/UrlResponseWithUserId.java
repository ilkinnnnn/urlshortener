package io.github.ilkinnnnn.urlshortener.model.response;

import java.time.LocalDateTime;

public record UrlResponseWithUserId(
        Long id,
        String originalUrl,
        String shortCode,
        Long clickCount,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime lastAccessedAt,
        LocalDateTime expiresAt
) {
}
