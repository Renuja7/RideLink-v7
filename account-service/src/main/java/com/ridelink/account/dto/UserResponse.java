package com.ridelink.account.dto;

import com.ridelink.account.entity.AccountStatus;
import com.ridelink.account.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Public-facing representation of a User, returned by controllers instead of the
 * User entity itself. Deliberately omits the password hash so it is never exposed
 * over the API, even by accident.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
