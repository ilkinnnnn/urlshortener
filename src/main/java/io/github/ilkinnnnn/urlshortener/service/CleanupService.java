package io.github.ilkinnnnn.urlshortener.service;

import io.github.ilkinnnnn.urlshortener.repository.RefreshTokenRepo;
import io.github.ilkinnnnn.urlshortener.repository.UrlRepo;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CleanupService {
    private final UrlRepo urlRepo;
    private final RefreshTokenRepo refreshTokenRepo;

    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void cleanUrl() {
        urlRepo.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }

    @Scheduled(fixedRate = 1000 * 60 * 60, initialDelay = 1000 * 60)
    public void cleanRefreshToken() {
        refreshTokenRepo.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }
}
