package com.echo.waste.service;
import com.echo.waste.dto.ApiDtos.*;
import com.echo.waste.entity.AppUser;
import com.echo.waste.enums.UserRole;
import com.echo.waste.exception.ApiException;
import com.echo.waste.repository.UserRepository;
import com.echo.waste.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class AuthService {
 private final UserRepository users;private final PasswordEncoder passwords;private final JwtService jwt;
 public AuthService(UserRepository users,PasswordEncoder passwords,JwtService jwt){this.users=users;this.passwords=passwords;this.jwt=jwt;}
 @Transactional public AuthResponse register(Register input){String email=input.email().trim().toLowerCase(java.util.Locale.ROOT);if(users.existsByEmailIgnoreCase(email))throw new ApiException(HttpStatus.CONFLICT,"This email already has an Echo account. Sign in instead.");AppUser user=users.save(new AppUser(input.fullName().trim(),email,passwords.encode(input.password()),clean(input.phone()),clean(input.address()),UserRole.CITIZEN));return response(user);}
 @Transactional(readOnly=true) public AuthResponse login(Login input){String email=input.email().trim();AppUser user=users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"Email or password is incorrect."));if(!passwords.matches(input.password(),user.getPassword()))throw new ApiException(HttpStatus.UNAUTHORIZED,"Email or password is incorrect.");return response(user);}
 private String clean(String value){return value==null||value.isBlank()?null:value.trim();}
 private AuthResponse response(AppUser user){return new AuthResponse(jwt.create(user),new UserView(user.getId(),user.getFullName(),user.getEmail(),user.getPhone(),user.getAddress(),user.getRole(),user.getCreatedAt()));}
}
