package io.github.ilkinnnnn.urlshortener.model.request;


import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
