package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.entity.AccountStatus;
import com.ridelink.account.entity.Role;
import com.ridelink.account.entity.User;
import com.ridelink.account.exception.DuplicateResourceException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.repository.UserRepository;
import com.ridelink.account.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John Doe", "john@example.com", "0771234567", "password123", Role.PASSENGER);

        user = User.builder()
                .id("1")
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("0771234567")
                .password("encodedPassword")
                .role(Role.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_shouldCreateUser_whenEmailNotTaken() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.PASSENGER, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("mocked-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("PASSENGER", response.getRole());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "wrongPassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        LoginRequest loginRequest = new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }
}
