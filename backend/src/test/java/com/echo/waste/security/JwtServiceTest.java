package com.echo.waste.security;

import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String VALID_SECRET = "super-secret-key-that-is-at-least-32-chars-long!";
    private static final long EXPIRATION_MS = 3600000;

    @Test
    @DisplayName("Should create token and extract subject accurately")
    void testCreateAndExtractSubject() {
        JwtService jwtService = new JwtService(VALID_SECRET, EXPIRATION_MS);
        AppUser user = new AppUser("Jane Doe", "jane@example.com", "hashed_pwd", "1234567890", "123 St", UserRole.CITIZEN);

        String token = jwtService.create(user);
        assertThat(token).isNotBlank();

        String subject = jwtService.subject(token);
        assertThat(subject).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("Should reject initialization if secret is shorter than 32 characters")
    void testShortSecretRejected() {
        assertThatThrownBy(() -> new JwtService("short-secret", EXPIRATION_MS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT_SECRET must contain at least 32 characters");
    }

    @Test
    @DisplayName("Should reject initialization if expiration is non-positive")
    void testNonPositiveExpirationRejected() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT_EXPIRATION_MS must be greater than zero");
    }
}
