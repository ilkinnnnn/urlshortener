package io.github.ilkinnnnn.urlshortener.service;

import io.github.ilkinnnnn.urlshortener.exception.InvalidShortCodeException;
import io.github.ilkinnnnn.urlshortener.exception.NotFoundException;
import io.github.ilkinnnnn.urlshortener.exception.ShortCodeAlreadyExistException;
import io.github.ilkinnnnn.urlshortener.exception.UnauthorizedException;
import io.github.ilkinnnnn.urlshortener.mapper.UrlMapper;
import io.github.ilkinnnnn.urlshortener.model.entity.Url;
import io.github.ilkinnnnn.urlshortener.model.entity.User;
import io.github.ilkinnnnn.urlshortener.model.request.CreateUrlRequest;
import io.github.ilkinnnnn.urlshortener.repository.UrlRepo;
import io.github.ilkinnnnn.urlshortener.repository.UserRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlServiceTest {
    @Mock
    UrlRepo urlRepo;
    @Mock
    UserRepo userRepo;
    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    ValueOperations<String, String> valueOperations;
    @Mock
    UrlMapper urlMapper;

    @InjectMocks
    UrlService urlService;

    @Nested
    class Create {
        @Test
        void shouldCreateUrlWithRandomShortCode() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com", null);
            User user = new User(1L, "name", "hashedPassword");
            ArgumentCaptor<Url> urlCaptor = ArgumentCaptor.forClass(Url.class);

            when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
            when(urlRepo.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

            urlService.create(user.getId(), request);

            verify(urlRepo).save(urlCaptor.capture());
            verify(urlMapper).urlResponse(urlCaptor.getValue());

            assertThat(urlCaptor.getValue())
                    .isNotNull()
                    .returns(request.originalUrl(), Url::getOriginalUrl)
                    .returns(user.getId(), url -> url.getUser().getId());

            assertThat(urlCaptor.getValue().getShortCode()).isNotBlank();
        }

        @Test
        void shouldCreateUrlWithSpecifiedShortCode() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com", "6char0");
            User user = new User(1L, "name", "hashedPassword");
            ArgumentCaptor<Url> urlCaptor = ArgumentCaptor.forClass(Url.class);

            when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
            when(urlRepo.existsByShortCode(request.shortCode())).thenReturn(false);
            when(urlRepo.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

            urlService.create(user.getId(), request);

            verify(urlRepo).save(urlCaptor.capture());
            verify(urlMapper).urlResponse(urlCaptor.getValue());

            assertThat(urlCaptor.getValue())
                    .isNotNull()
                    .returns(request.originalUrl(), Url::getOriginalUrl)
                    .returns(user.getId(), url -> url.getUser().getId())
                    .returns(request.shortCode(), Url::getShortCode);

        }

        @Test
        void shouldThrowInvalidShortCode() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com", "bigger than6");
            User user = new User(1L, "name", "hashedPassword");

            when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> urlService.create(user.getId(), request))
                    .isInstanceOf(InvalidShortCodeException.class);
        }

        @Test
        void shouldThrowShortCodeAlreadyExists() {
            CreateUrlRequest request = new CreateUrlRequest("https://google.com", "valid6");
            User user = new User(1L, "name", "hashedPassword");

            when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
            when(urlRepo.existsByShortCode(request.shortCode())).thenReturn(true);

            assertThatThrownBy(() -> urlService.create(user.getId(), request))
                    .isInstanceOf(ShortCodeAlreadyExistException.class);
        }
    }

    @Nested
    class GetOriginalUrl {
        @Test
        void shouldGetOriginalUrlFromRedisAndSetInfo() {
            String originalUrl = "https://google.com";
            String shortCode = "shortC";

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(shortCode)).thenReturn(originalUrl);
            when(valueOperations.get("expires: " + shortCode)).thenReturn(LocalDateTime.now().plusHours(1).toString());

            String result = urlService.getOriginalUrl(shortCode);

            verify(valueOperations).increment("clicks: " + shortCode);
            verify(valueOperations).set(eq("lat: " + shortCode), anyString());

            assertThat(result).isEqualTo(originalUrl);
        }

        @Test
        void shouldGetOriginalUrlFromDbAndSetToRedis() {
            String originalUrl = "https://google.com";
            String shortCode = "shortC";
            Url url = new Url(
                    1L, originalUrl, shortCode, 0L,
                    null, LocalDateTime.now().plusHours(1),
                    null, null, null
            );

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(shortCode)).thenReturn(null);
            when(urlRepo.findByShortCode(shortCode)).thenReturn(Optional.of(url));

            String result = urlService.getOriginalUrl(shortCode);
            assertThat(result).isEqualTo(originalUrl);

            verify(valueOperations).set(shortCode, originalUrl, Duration.ofHours(24));
            verify(valueOperations).set("expires: " + shortCode, url.getExpiresAt().toString());
            verify(valueOperations).increment("clicks: " + shortCode);
            verify(valueOperations).set(eq("lat: " + shortCode), anyString());
        }

        @Test
        void shouldThrowNotFound() {
            String shortCode = "shortC";

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(shortCode)).thenReturn(null);
            when(urlRepo.findByShortCode(shortCode)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> urlService.getOriginalUrl(shortCode))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void shouldThrowNotFoundOnExpire() {
            String originalUrl = "https://google.com";
            String shortCode = "shortC";
            Url url = new Url(
                    1L, originalUrl, shortCode, 0L,
                    null, LocalDateTime.now().minusSeconds(1),
                    null, null, null
            );

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(shortCode)).thenReturn(null);
            when(urlRepo.findByShortCode(shortCode)).thenReturn(Optional.of(url));

            assertThatThrownBy(() -> urlService.getOriginalUrl(shortCode))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class Delete {
        @Test
        void shouldDeleteFromDbAndRedis() {
            Long userId = 1L;
            String shortCode = "shortC";
            Url url = new Url(
                    1L, "originalUrl", shortCode, 0L,
                    null, null,
                    null, null, userId
            );

            when(urlRepo.findByShortCode(shortCode)).thenReturn(Optional.of(url));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            urlService.delete(shortCode,  userId);

            verify(urlRepo).delete(url);
            verify(valueOperations).getAndDelete(shortCode);
        }

        @Test
        void shouldThrowNotFound() {
            String shortCode = "shortC";

            when(urlRepo.findByShortCode(shortCode)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> urlService.delete(shortCode,  0L))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void shouldThrowUnauthorized() {
            Long userId = 1L;
            String shortCode = "shortC";
            Url url = new Url(
                    1L, "originalUrl", shortCode, 0L,
                    null, null,
                    null, null, 9L
            );

            when(urlRepo.findByShortCode(shortCode)).thenReturn(Optional.of(url));

            assertThatThrownBy(() -> urlService.delete(shortCode,  userId))
                .isInstanceOf(UnauthorizedException.class);
        }
    }
}
