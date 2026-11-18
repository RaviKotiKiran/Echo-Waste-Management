package com.echo.waste.config;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import com.echo.waste.repository.UserRepository;
import com.echo.waste.repository.WasteRequestRepository;
import com.echo.waste.entity.WasteRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
@Configuration @Profile("mysql")
public class DevDataInitializer {
 @Bean CommandLineRunner seedDemoUsers(UserRepository users,WasteRequestRepository requests,PasswordEncoder encoder){return args->{create(users,encoder,"Echo Administrator","admin@echo.local","Admin@123",UserRole.ADMIN);create(users,encoder,"Demo Collector","collector@echo.local","Collector@123",UserRole.COLLECTOR);create(users,encoder,"Demo Citizen","citizen@echo.local","Citizen@123",UserRole.CITIZEN);if(requests.count()==0){var citizen=users.findByEmailIgnoreCase("citizen@echo.local").orElseThrow();requests.save(new WasteRequest(citizen,"Household Waste",new BigDecimal("2.50"),"12 Green Street, Example City",LocalDate.now().plusDays(1),LocalTime.of(10,0),"Demo request: please collect from the front gate."));}};}
 private void create(UserRepository users,PasswordEncoder encoder,String name,String email,String password,UserRole role){if(!users.existsByEmailIgnoreCase(email))users.save(new AppUser(name,email.toLowerCase(Locale.ROOT),encoder.encode(password),null,null,role));}
}
