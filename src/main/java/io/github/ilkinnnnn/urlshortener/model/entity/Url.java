package io.github.ilkinnnnn.urlshortener.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Url {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    private Long clickCount = 0L;

    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime lastAccessedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(name = "users_id", insertable = false, updatable = false)
    private Long userId;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        lastAccessedAt = now;
    }
}
