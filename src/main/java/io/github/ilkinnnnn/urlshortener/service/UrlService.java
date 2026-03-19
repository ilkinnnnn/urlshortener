package io.github.ilkinnnnn.urlshortener.service;

import io.github.ilkinnnnn.urlshortener.exception.NotFoundException;
import io.github.ilkinnnnn.urlshortener.exception.UnauthorizedException;
import io.github.ilkinnnnn.urlshortener.mapper.UrlMapper;
import io.github.ilkinnnnn.urlshortener.model.AuthenticatedUser;
import io.github.ilkinnnnn.urlshortener.model.entity.Url;
import io.github.ilkinnnnn.urlshortener.model.entity.User;
import io.github.ilkinnnnn.urlshortener.model.request.CreateUrlRequest;
import io.github.ilkinnnnn.urlshortener.model.response.PageResponse;
import io.github.ilkinnnnn.urlshortener.model.response.UrlResponse;
import io.github.ilkinnnnn.urlshortener.repository.UrlRepo;
import io.github.ilkinnnnn.urlshortener.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Service
@AllArgsConstructor
public class UrlService {
    private final UrlRepo urlRepo;
    private final UserRepo userRepo;
    private final StringRedisTemplate redisTemplate;
    private final UrlMapper urlMapper;

    @Transactional
    public UrlResponse create(Long userId, CreateUrlRequest request) {
        User user = userRepo
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String shortCode = getRandomShortCode();

        Url url = new Url();
        url.setOriginalUrl(request.originalUrl());
        url.setShortCode(shortCode);
        url.setUser(user);
        urlRepo.save(url);
        return urlMapper.urlResponse(url);
    }

    public String getOriginalUrl(String shortCode) {
        String originalUrl = redisTemplate.opsForValue().get(shortCode);
        if (originalUrl != null) {
            setUrlInfoToRedis(shortCode);
            return originalUrl;
        }

        Url url = urlRepo.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("short code not found"));

        redisTemplate.opsForValue().set(shortCode, url.getOriginalUrl(), Duration.ofHours(24));
        setUrlInfoToRedis(shortCode);
        return url.getOriginalUrl();
    }

    public PageResponse<UrlResponse> getAll(Jwt jwt, Pageable pageable) {
        AuthenticatedUser aUser = new AuthenticatedUser(jwt);
        Page<Url> result;
//        if (aUser.isAdmin()) {
//            result = urlRepo.findAll(pageable);
//        }
        result = urlRepo.findAllByUserId(aUser.id(), pageable);

        return new PageResponse<>(result.map(urlMapper::urlResponse));
    }

    @Transactional
    public UrlResponse getStats(String shortCode, Long userId) {
        Url url = urlRepo.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("short code not found"));

        if (!Objects.equals(url.getUserId(), userId)) {
            throw new UnauthorizedException();
        }

        String clickCountS = redisTemplate.opsForValue().get("clicks: " + shortCode);
        if (clickCountS != null) {
            Long clickCount = Long.valueOf(clickCountS);
            url.setClickCount(clickCount);
        }

        String latS = redisTemplate.opsForValue().get("lat: " + shortCode);
        if (latS != null) {
            LocalDateTime lat = LocalDateTime.parse(latS);
            url.setLastAccessedAt(lat);
        }

        return urlMapper.urlResponse(url);
    }

    @Transactional
    public void delete(String shortCode, Long userId) {
        Url url = urlRepo.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("short code not found"));

        if (!url.getUserId().equals(userId)) {
            throw new UnauthorizedException();
        }

        urlRepo.delete(url);
        redisTemplate.opsForValue().getAndDelete(shortCode);
    }

    private String getRandomShortCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 6; i ++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }

        return result.toString();
    }

    private void setUrlInfoToRedis(String shortCode) {
        redisTemplate.opsForValue().increment("clicks: " + shortCode);
        redisTemplate.opsForValue().set("lat: " + shortCode, LocalDateTime.now().toString());
    }

}
