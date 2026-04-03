package io.github.ilkinnnnn.urlshortener.util;

import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtUtil {
    private final JwtEncoder jwtEncoder;

    public String generateToken(Long userId, Boolean isAdmin) {
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        Instant now = Instant.now();
        List<String> roles = isAdmin ? List.of("ADMIN") : List.of("USER");

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(userId.toString())
                .claim("roles", roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String sha256(String plain) {
       try {
           MessageDigest digest = MessageDigest.getInstance("SHA-256");
           byte[] hashBytes = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
           return Base64.getEncoder().encodeToString(hashBytes);
       } catch (NoSuchAlgorithmException e) {
           throw new RuntimeException("SHA-256 not available to hash refresh tokens", e);
       }
    }
}
