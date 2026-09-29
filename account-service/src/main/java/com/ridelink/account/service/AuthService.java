package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.entity.AccountStatus;
import com.ridelink.account.entity.User;
import com.ridelink.account.exception.DuplicateResourceException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.repository.UserRepository;
import com.ridelink.account.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core business logic for registration and login — the heart of this service's
 * "account and access" responsibility. Keeps password hashing, credential checking
 * and JWT issuance in one place so controllers stay thin.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a brand-new account. Rejects the request if the email is already in
     * use (business rule: email is the unique login identifier), hashes the plain-text
     * password with BCrypt before it is ever stored, defaults the new account's status
     * to ACTIVE, and returns the created user's public profile.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            // Prevents two accounts from sharing a login email.
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword())) // Never store the raw password, only its BCrypt hash
                .role(request.getRole())
                .status(AccountStatus.ACTIVE) // New accounts can log in immediately
                .build();

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    /**
     * Authenticates a user and, on success, issues a JWT.
     * Enforces three business rules in order: the email must exist, the supplied
     * password must match the stored hash, and the account's status must be ACTIVE
     * (SUSPENDED/INACTIVE accounts are blocked from logging in even with the right
     * password). Uses the same generic error message for "no such email" and "wrong
     * password" so an attacker can't use the error to guess which emails are registered.
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // BCrypt comparison: re-hashes the supplied password with the stored salt and
        // compares the result, rather than ever decrypting the stored hash.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            // Business rule: suspended/inactive accounts cannot authenticate even with correct credentials.
            throw new InvalidCredentialsException("Account is not active");
        }

        // Embed the user's id and role in the token so other services can trust them
        // without a callback to this service.
        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    // Maps the internal User entity to the public UserResponse DTO, making sure the
    // password hash never leaves this service.
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
