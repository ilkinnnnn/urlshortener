package io.github.ilkinnnnn.urlshortener.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SoftDelete(columnName = "revoked")
public class RefreshToken {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private String tokenHash;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoggedIn;

    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(name = "users_id", insertable = false, updatable = false)
    private Long userId;
}
