package io.github.ilkinnnnn.urlshortener.repository;

import io.github.ilkinnnnn.urlshortener.model.entity.Url;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepo extends JpaRepository<Url, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void increaseClickCount(String shortCode);

    Optional<Url> findByShortCode(String shortCode);

    Page<Url> findAllByUserId(Long id, Pageable pageable);
    void deleteAllByExpiresAtBefore(LocalDateTime expiresAt);
}
