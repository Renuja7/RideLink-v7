package com.ridelink.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/users/{id}.
 * Represents the subset of profile fields a user is allowed to self-edit (name, phone).
 * Deliberately excludes email, password, role and status, which each have their own
 * dedicated, more controlled flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}
