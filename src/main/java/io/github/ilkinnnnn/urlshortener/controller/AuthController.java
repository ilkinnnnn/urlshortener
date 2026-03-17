package io.github.ilkinnnnn.urlshortener.controller;

import io.github.ilkinnnnn.urlshortener.model.request.AuthRequest;
import io.github.ilkinnnnn.urlshortener.model.request.RefreshTokenRequest;
import io.github.ilkinnnnn.urlshortener.model.response.AuthResponse;
import io.github.ilkinnnnn.urlshortener.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid AuthRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody @Valid RefreshTokenRequest refreshToken) {
        return authService.refresh(refreshToken.token());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid RefreshTokenRequest refreshToken
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.logout(userId, refreshToken.token());
    }
}
