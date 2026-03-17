package io.github.ilkinnnnn.urlshortener.model.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank String token
) {
}
