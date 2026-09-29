package com.ridelink.account.controller;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the public authentication endpoints: account registration
 * and login. These endpoints are intentionally left open (no JWT required) in
 * SecurityConfig, because a user obviously cannot present a token before they have
 * one.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // Lombok generates a constructor that injects authService (no @Autowired needed)
@Tag(name = "Authentication", description = "Registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new passenger, driver or admin account.
     * Accepts a validated RegisterRequest (name, email, phone, password, role), delegates
     * the actual account-creation logic (uniqueness check, password hashing) to
     * AuthService, and returns the created user's public profile with HTTP 201 Created.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new passenger, driver or admin account")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates a user with email + password and issues a signed JWT token on success.
     * The returned token carries the user's id and role as claims so that the other
     * microservices can trust it without calling back into Account Service.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and issue a JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
