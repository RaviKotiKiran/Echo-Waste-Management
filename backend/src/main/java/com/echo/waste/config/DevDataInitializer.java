package com.echo.waste.config;

import com.echo.waste.entity.AppUser;
import com.echo.waste.entity.WasteRequest;
import com.echo.waste.enums.UserRole;
import com.echo.waste.repository.UserRepository;
import com.echo.waste.repository.WasteRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

@Configuration
@Profile("!prod")
public class DevDataInitializer {

    @Bean
    public CommandLineRunner seedDemoUsers(UserRepository users, WasteRequestRepository requests, PasswordEncoder encoder) {
        return args -> {
            create(users, encoder, "Echo Administrator", "admin@echo.local", "Admin@123", UserRole.ADMIN);
            create(users, encoder, "Demo Collector", "collector@echo.local", "Collector@123", UserRole.COLLECTOR);
            create(users, encoder, "Demo Citizen", "citizen@echo.local", "Citizen@123", UserRole.CITIZEN);

            if (requests.count() == 0) {
                var citizen = users.findByEmailIgnoreCase("citizen@echo.local");
                if (citizen.isPresent()) {
                    requests.save(new WasteRequest(
                            citizen.get(),
                            "Household Waste",
                            new BigDecimal("2.50"),
                            "12 Green Street, Example City",
                            LocalDate.now().plusDays(1),
                            LocalTime.of(10, 0),
                            "Demo request: please collect from the front gate."
                    ));
                }
            }
        };
    }

    private void create(UserRepository users, PasswordEncoder encoder, String name, String email, String password, UserRole role) {
        if (!users.existsByEmailIgnoreCase(email)) {
            users.save(new AppUser(name, email.toLowerCase(Locale.ROOT), encoder.encode(password), null, null, role));
        }
    }
}
