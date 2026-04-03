package io.github.ilkinnnnn.urlshortener.service;

import io.github.ilkinnnnn.urlshortener.config.AdminProperties;
import io.github.ilkinnnnn.urlshortener.exception.UnauthorizedException;
import io.github.ilkinnnnn.urlshortener.exception.UsernameAlreadyExist;
import io.github.ilkinnnnn.urlshortener.model.entity.RefreshToken;
import io.github.ilkinnnnn.urlshortener.model.entity.User;
import io.github.ilkinnnnn.urlshortener.model.request.AuthRequest;
import io.github.ilkinnnnn.urlshortener.model.response.AuthResponse;
import io.github.ilkinnnnn.urlshortener.model.response.UserResponse;
import io.github.ilkinnnnn.urlshortener.repository.RefreshTokenRepo;
import io.github.ilkinnnnn.urlshortener.repository.UserRepo;
import io.github.ilkinnnnn.urlshortener.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AdminProperties adminProperties;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        if (request.username().equals(adminProperties.getUsername())
                || userRepo.existsByUsername(request.username())) {
            throw new UsernameAlreadyExist();
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepo.save(user);

        String refreshToken = createRefreshToken(user);
        String accessToken = jwtUtil.generateToken(user.getId(), false);


        return new AuthResponse(
                new UserResponse(user.getId(), user.getUsername()),
                accessToken,
                refreshToken
        );
    }

    public AuthResponse login(AuthRequest request) {
        if (request.username().equals(adminProperties.getUsername())) {
            if (passwordEncoder.matches(request.password(), adminProperties.getPassword())) {
                return new AuthResponse(
                        null,
                        jwtUtil.generateToken(0L, true),
                        "no refresh for admin"
                );
            }
            throw new BadCredentialsException("Bad credentials");
        }

        User user = userRepo.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }

        String refreshToken = createRefreshToken(user);
        String accessToken = jwtUtil.generateToken(user.getId(), false);

        return new AuthResponse(
                new UserResponse(user.getId(), user.getUsername()),
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public AuthResponse refresh(String token) {
        RefreshToken refreshToken = refreshTokenRepo
                .findByTokenHash(jwtUtil.sha256(token))
                .orElseThrow(UnauthorizedException::new);

        if (LocalDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            throw new UnauthorizedException();
        }

        User user = refreshToken.getUser();

        String newRefreshToken = UUID.randomUUID().toString();
        refreshToken.setTokenHash(jwtUtil.sha256(newRefreshToken));
        refreshToken.setLastLoggedIn(LocalDateTime.now());

        String accessToken = jwtUtil.generateToken(user.getId(), false);

        return new AuthResponse(
                new UserResponse(user.getId(), user.getUsername()),
                accessToken,
                newRefreshToken
        );
    }

    public void logout(Long userId, String token) {
        RefreshToken refreshToken = refreshTokenRepo
                .findByTokenHash(jwtUtil.sha256(token))
                .orElseThrow(UnauthorizedException::new);

        if (!userId.equals(refreshToken.getUserId())) {
            throw new UnauthorizedException();
        }

        refreshTokenRepo.delete(refreshToken);
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(jwtUtil.sha256(token));
        refreshToken.setCreatedAt(now);
        refreshToken.setLastLoggedIn(now);
        refreshToken.setExpiresAt(now.plusDays(30));
        refreshToken.setUser(user);
        refreshTokenRepo.save(refreshToken);

        return token;
    }
}
