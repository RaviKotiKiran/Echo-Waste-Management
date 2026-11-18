package com.echo.waste.config;

import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import com.echo.waste.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Configuration
@Profile("prod")
public class ProductionAdminBootstrap {
    @Bean
    CommandLineRunner createInitialAdministrator(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            org.springframework.core.env.Environment environment) {
        return args -> {
            String email = environment.getProperty("BOOTSTRAP_ADMIN_EMAIL");
            String password = environment.getProperty("BOOTSTRAP_ADMIN_PASSWORD");
            if (!StringUtils.hasText(email) && !StringUtils.hasText(password)) return;
            if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
                throw new IllegalStateException("Set both BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD for initial admin setup.");
            }
            if (password.length() < 12) {
                throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be at least 12 characters.");
            }

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            var existing = users.findByEmailIgnoreCase(normalizedEmail);
            if (existing.isPresent()) {
                if (existing.get().getRole() != UserRole.ADMIN) {
                    throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL already belongs to a non-admin account.");
                }
                return;
            }

            String name = environment.getProperty("BOOTSTRAP_ADMIN_NAME", "Echo Administrator").trim();
            users.save(new AppUser(name, normalizedEmail, passwordEncoder.encode(password), null, null, UserRole.ADMIN));
        };
    }
}
