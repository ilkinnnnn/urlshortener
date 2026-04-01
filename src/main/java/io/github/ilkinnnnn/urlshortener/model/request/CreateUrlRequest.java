package io.github.ilkinnnnn.urlshortener.model.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateUrlRequest(
        @NotBlank @URL String originalUrl,
        String shortCode
) {
}
