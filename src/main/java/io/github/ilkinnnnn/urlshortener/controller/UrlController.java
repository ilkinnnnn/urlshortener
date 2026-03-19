package io.github.ilkinnnnn.urlshortener.controller;

import io.github.ilkinnnnn.urlshortener.model.request.CreateUrlRequest;
import io.github.ilkinnnnn.urlshortener.model.response.PageResponse;
import io.github.ilkinnnnn.urlshortener.model.response.UrlResponse;
import io.github.ilkinnnnn.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/short-urls")
public class UrlController {
    private final UrlService urlService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UrlResponse create(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CreateUrlRequest request) {
        Long id = Long.valueOf(jwt.getSubject());
        return urlService.create(id, request);
    }

    @GetMapping
    public PageResponse<UrlResponse> getAll(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        return urlService.getAll(jwt, pageable);
    }

    @GetMapping("/{shortCode}/stats")
    public UrlResponse getStats(@PathVariable String shortCode, @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return urlService.getStats(shortCode, userId);
    }

    @DeleteMapping("/{shortCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String shortCode, @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        urlService.delete(shortCode, userId);
    }

}
