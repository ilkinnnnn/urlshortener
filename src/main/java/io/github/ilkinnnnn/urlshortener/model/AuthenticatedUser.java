package io.github.ilkinnnnn.urlshortener.model;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public record AuthenticatedUser(
        Long id,
        List<String> roles
) {
    public AuthenticatedUser(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null) {
            throw new RuntimeException("access token do not contain sub!");
        }

        this(
                Long.valueOf(sub),
                jwt.getClaimAsStringList("roles")
        );
    }

    public Boolean isAdmin() {
        return roles.contains("ROLE_ADMIN");
    }
}
