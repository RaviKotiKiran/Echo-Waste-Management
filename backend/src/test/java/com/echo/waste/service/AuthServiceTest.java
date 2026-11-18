package com.echo.waste.service;

import com.echo.waste.dto.ApiDtos.AuthResponse;
import com.echo.waste.dto.ApiDtos.Login;
import com.echo.waste.dto.ApiDtos.Register;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import com.echo.waste.exception.ApiException;
import com.echo.waste.repository.UserRepository;
import com.echo.waste.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private final Map<String, AppUser> database = new HashMap<>();
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        database.clear();

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("existsByEmailIgnoreCase".equals(name)) {
                        String email = (String) args[0];
                        return database.containsKey(email.toLowerCase());
                    }
                    if ("findByEmailIgnoreCase".equals(name)) {
                        String email = (String) args[0];
                        return Optional.ofNullable(database.get(email.toLowerCase()));
                    }
                    if ("save".equals(name)) {
                        AppUser user = (AppUser) args[0];
                        database.put(user.getEmail().toLowerCase(), user);
                        return user;
                    }
                    return null;
                });

        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("a-very-secure-random-jwt-secret-key-at-least-32-chars", 3600000);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Should successfully register a new citizen and generate valid JWT")
    void testRegisterSuccess() {
        Register input = new Register("John Citizen", "john@example.com", "Secret@123", "9876543210", "123 Green Way");

        AuthResponse response = authService.register(input);

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("john@example.com");
        assertThat(response.user().role()).isEqualTo(UserRole.CITIZEN);
        assertThat(database).containsKey("john@example.com");

        // Verify password was hashed with BCrypt
        AppUser savedUser = database.get("john@example.com");
        assertThat(passwordEncoder.matches("Secret@123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Should reject registration if email is already taken")
    void testRegisterDuplicateEmail() {
        Register input = new Register("John Citizen", "john@example.com", "Secret@123", "9876543210", "123 Green Way");
        authService.register(input);

        Register duplicate = new Register("John Citizen", "john@example.com", "AnotherSecret@123", null, null);

        assertThatThrownBy(() -> authService.register(duplicate))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(apiEx.getMessage()).contains("already has an Echo account");
                });
    }

    @Test
    @DisplayName("Should successfully authenticate valid user credentials")
    void testLoginSuccess() {
        Register input = new Register("John Citizen", "john@example.com", "Secret@123", null, null);
        authService.register(input);

        Login login = new Login("john@example.com", "Secret@123");
        AuthResponse response = authService.login(login);

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("john@example.com");
        assertThat(jwtService.subject(response.token())).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should reject login if email does not exist")
    void testLoginNonExistentEmail() {
        Login input = new Login("nonexistent@example.com", "Secret@123");

        assertThatThrownBy(() -> authService.login(input))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should reject login if password is incorrect")
    void testLoginIncorrectPassword() {
        Register input = new Register("John Citizen", "john@example.com", "Secret@123", null, null);
        authService.register(input);

        Login inputWrongPassword = new Login("john@example.com", "WrongPassword");

        assertThatThrownBy(() -> authService.login(inputWrongPassword))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
