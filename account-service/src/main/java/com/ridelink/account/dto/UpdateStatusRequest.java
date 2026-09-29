package com.ridelink.account.dto;

import com.ridelink.account.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/users/{id}/status.
 * Used exclusively by the admin-only "account status management" endpoint to
 * activate, deactivate or suspend a user's account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private AccountStatus status; // The new status (ACTIVE/INACTIVE/SUSPENDED) to apply to the target user
}
