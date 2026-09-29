package com.ridelink.account.dto;

import com.ridelink.account.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/users/{id}/role.
 * Used exclusively by the admin-only "role management" endpoint to change which role
 * (PASSENGER/DRIVER/ADMIN) a given user account holds.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @NotNull(message = "role is required")
    private Role role; // The new role to assign to the target user
}
