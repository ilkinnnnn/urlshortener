package io.github.ilkinnnnn.urlshortener.model.response;


public record AuthResponse(
        UserResponse user,
        String accessToken,
        String refreshToken
) {
}
