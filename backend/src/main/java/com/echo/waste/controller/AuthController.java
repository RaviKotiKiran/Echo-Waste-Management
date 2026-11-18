package com.echo.waste.controller;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService auth;public AuthController(AuthService auth){this.auth=auth;}
 @PostMapping("/register") public AuthResponse register(@Valid @RequestBody Register input){return auth.register(input);}
 @PostMapping("/login") public AuthResponse login(@Valid @RequestBody Login input){return auth.login(input);}
}
