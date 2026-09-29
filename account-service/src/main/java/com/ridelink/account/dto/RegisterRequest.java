package com.ridelink.account.dto;

import com.ridelink.account.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/auth/register.
 * Captures everything needed to create a new passenger, driver or admin account.
 * Bean Validation annotations enforce the assignment's "validation" requirement and
 * cause Spring to return a 400 Bad Request with a clear message before the service
 * layer is even invoked.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email; // Used as the unique login identifier; uniqueness is enforced in AuthService/DB

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters") // Basic strength rule for this simulated system
    private String password; // Raw password from the client; hashed with BCrypt before it is ever persisted

    @NotNull(message = "Role is required")
    private Role role; // PASSENGER, DRIVER or ADMIN — determines what the account can do
}
