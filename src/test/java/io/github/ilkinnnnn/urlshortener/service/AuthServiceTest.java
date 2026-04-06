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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepo userRepo;
    @Mock
    private RefreshTokenRepo refreshTokenRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    private AdminProperties adminProperties;

    private AuthService authService;

    @BeforeEach
    public void setup() {
        adminProperties = new AdminProperties("admin", "hashedPass");
        authService = new AuthService(
                userRepo,
                refreshTokenRepo,
                passwordEncoder,
                jwtUtil,
                adminProperties
        );
    }

    @Nested
    class Register {
        @Test
        void shouldCreateUserAndRefreshToken() {
            String hashedPassword = "hashed";
            String accessToken = "token";
            String hashedRefreshToken = "hashedToken";

            AuthRequest request = new AuthRequest("username", "password");
            User user = new User(1L, request.username(), hashedPassword);
            UserResponse userResponse = new UserResponse(user.getId(), request.username());

            when(userRepo.existsByUsername(request.username())).thenReturn(Boolean.FALSE);
            when(userRepo.save(any(User.class))).thenReturn(user);
            when(passwordEncoder.encode(request.password())).thenReturn(hashedPassword);
            when(jwtUtil.generateToken(user.getId(), false)).thenReturn(accessToken);
            when(jwtUtil.sha256(any(String.class))).thenReturn(hashedRefreshToken);
            when(refreshTokenRepo.save(any(RefreshToken.class))).thenReturn(null);

            AuthResponse response = authService.register(request);

            assertThat(response)
                    .isNotNull()
                    .returns(accessToken, AuthResponse::accessToken)
                    .returns(userResponse, AuthResponse::user);

            verify(userRepo).existsByUsername(request.username());
            verify(userRepo).save(argThat(
                    u -> u.getUsername().equals(request.username()) &&
                            u.getPassword().equals(hashedPassword)
            ));
            verify(jwtUtil).sha256(anyString());
            verify(refreshTokenRepo).save(any(RefreshToken.class));
        }

        @Test
        void shouldThrowUsernameAlreadyExistsAndNotCallRepo() {
            AuthRequest request = new AuthRequest("admin", "password");

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(UsernameAlreadyExist.class);

            verify(userRepo, never()).existsByUsername(request.username());
            verify(userRepo, never()).save(any(User.class));
            verify(refreshTokenRepo, never()).save(any(RefreshToken.class));
        }

        @Test
        void shouldThrowUsernameAlreadyExistsAndNotCallRepoSave() {
            AuthRequest request = new AuthRequest("user", "password");
            when(userRepo.existsByUsername(request.username())).thenReturn(Boolean.TRUE);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(UsernameAlreadyExist.class);

            verify(userRepo, never()).save(any(User.class));
            verify(refreshTokenRepo, never()).save(any(RefreshToken.class));
        }
    }

    @Nested
    class Login {

        @Test
        void shouldLoginAdmin() {
            AuthRequest request = new AuthRequest(adminProperties.getUsername(), "admin");

            when(passwordEncoder.matches(request.password(), adminProperties.getPassword())).thenReturn(true);
            when(jwtUtil.generateToken(0L, true)).thenReturn("token");

            AuthResponse response = authService.login(request);
            assertThat(response)
                    .isNotNull()
                    .returns(null, AuthResponse::user)
                    .extracting(AuthResponse::accessToken, AuthResponse::refreshToken)
                    .doesNotContainNull();

            verify(userRepo, never()).findByUsername(any(String.class));
        }

        @Test
        void shouldThrowBadCredentialsOnWrongAdminPassword() {
            AuthRequest request = new AuthRequest(adminProperties.getUsername(), "password");

            when(passwordEncoder.matches(request.password(), adminProperties.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(userRepo, never()).findByUsername(any(String.class));
        }

        @Test
        void shouldThrowBadCredentialsOnUsernameNotFound() {
            AuthRequest request = new AuthRequest("user", "password");

            when(userRepo.findByUsername(request.username())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

        }

        @Test
        void shouldThrowBadCredentialsOnWrongPassword() {
            AuthRequest request = new AuthRequest("user", "password");
            User user = new User(1L, request.username(), "password");

            when(userRepo.findByUsername(request.username())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        void shouldLoginUser() {
            AuthRequest request = new AuthRequest("user", "password");
            User user = new User(1L, request.username(), "hashedPassword");
            String accessToken = "accessToken";
            UserResponse userResponse = new UserResponse(user.getId(), user.getUsername());

            when(userRepo.findByUsername(request.username())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
            when(jwtUtil.sha256(anyString())).thenReturn("hashedToken");
            when(refreshTokenRepo.save(any(RefreshToken.class))).thenReturn(null);
            when(jwtUtil.generateToken(user.getId(), false)).thenReturn(accessToken);

            AuthResponse response = authService.login(request);

            assertThat(response)
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .returns(accessToken, AuthResponse::accessToken)
                    .returns(userResponse, AuthResponse::user);

            verify(jwtUtil).sha256(anyString());
            verify(refreshTokenRepo).save(any(RefreshToken.class));
        }
    }

    @Nested
    class Refresh{
        @Test
        void shouldRefresh() {
            String token = "refreshToken";
            String hashedToken = "hashedRefreshToken";
            String accessToken = "accessToken";

            User user = new User(1L, "name", "hashedPassword");
            UserResponse userResponse = new UserResponse(user.getId(), user.getUsername());
            RefreshToken refreshToken = new RefreshToken(
                    1L,
                    hashedToken,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(1),
                    user,
                    user.getId()
            );

            when(jwtUtil.sha256(token)).thenReturn(hashedToken);
            when(refreshTokenRepo.findByTokenHash(hashedToken)).thenReturn(Optional.of(refreshToken));
            when(jwtUtil.generateToken(user.getId(), false)).thenReturn(accessToken);

            AuthResponse response = authService.refresh(token);

            assertThat(response)
                    .isNotNull()
                    .returns(accessToken, AuthResponse::accessToken)
                    .returns(userResponse, AuthResponse::user)
                    .hasNoNullFieldsOrProperties();
        }

        @Test
        void shouldThrowUnauthorizedOnExpire(){
            String token = "refreshToken";
            String hashedToken = "hashedRefreshToken";

            User user = new User(1L, "name", "hashedPassword");
            RefreshToken refreshToken = new RefreshToken(
                    1L,
                    hashedToken,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now().minusSeconds(1),
                    user,
                    user.getId()
            );

            when(jwtUtil.sha256(token)).thenReturn(hashedToken);
            when(refreshTokenRepo.findByTokenHash(hashedToken)).thenReturn(Optional.of(refreshToken));

            assertThatThrownBy(() -> authService.refresh(token))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        void shouldThrowUnauthorizedOnInvalidRefreshToken() {
            String token = "token";
            String hashedToken = "hashedToken";

            when(jwtUtil.sha256(token)).thenReturn(hashedToken);
            when(refreshTokenRepo.findByTokenHash(hashedToken)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService
                    .refresh(token))
                    .isInstanceOf(UnauthorizedException.class);

            verify(jwtUtil).sha256(token);
            verify(refreshTokenRepo).findByTokenHash(hashedToken);
        }

    }

    @Nested
    class Logout {
        @Test
        void shouldDelete() {
            Long userId = 1L;
            String token = "token";
            String hashedToken = "hashedToken";

            RefreshToken refreshToken = new RefreshToken(
                    1L,
                    hashedToken,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(1),
                    null,
                    userId
            );

            when(jwtUtil.sha256(token)).thenReturn(hashedToken);
            when(refreshTokenRepo.findByTokenHash(hashedToken)).thenReturn(Optional.of(refreshToken));

            authService.logout(1L, token);

            verify(refreshTokenRepo).findByTokenHash(hashedToken);
            verify(refreshTokenRepo).delete(refreshToken);
        }

        @Test
        void shouldThrowUnauthorizedOnInvalidRefreshToken() {
            String token = "token";
            String hashedToken = "hashedToken";

            when(jwtUtil.sha256(token)).thenReturn(hashedToken);
            when(refreshTokenRepo.findByTokenHash(hashedToken)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService
                    .logout(1L, "token"))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }
}

