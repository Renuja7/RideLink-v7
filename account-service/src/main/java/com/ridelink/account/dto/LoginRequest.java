package com.ridelink.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/auth/login.
 * Carries the credentials a passenger/driver/admin submits to authenticate; validation
 * annotations ensure bad input is rejected with a 400 before it ever reaches AuthService.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid") // Rejects malformed emails early, before a DB lookup is attempted
    private String email;

    @NotBlank(message = "Password is required")
    private String password; // Plain-text password as typed by the user; never stored, only compared via BCrypt
}
