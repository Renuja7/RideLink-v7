package com.ridelink.account.controller;

import com.ridelink.account.dto.UpdateRoleRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.dto.UpdateUserRequest;
import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for viewing and managing user profiles, roles and account status.
 * All endpoints here require an authenticated request (a valid Bearer JWT) as configured
 * in SecurityConfig; the admin-only endpoints are additionally guarded with
 * {@code @PreAuthorize("hasRole('ADMIN')")}.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    /**
     * Fetches a single user's public profile by their id.
     * Throws a 404-mapped ResourceNotFoundException (via UserService) if no such user exists.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Updates a user's editable profile fields (full name and phone number).
     * Does not allow changing email, password, role or status here — those are
     * handled by dedicated, more tightly controlled endpoints/flows.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user's profile")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                     @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Lists every registered user. Restricted to ADMIN role because it exposes data
     * about all passengers and drivers, not just the caller's own account.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only an authenticated ADMIN may list all accounts
    @Operation(summary = "Get all users (admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Admin-only endpoint that changes a user's role (PASSENGER/DRIVER/ADMIN).
     * This exists specifically to satisfy the "role management" requirement in the
     * assignment brief, separate from normal profile editing, since role changes are
     * a privileged administrative action rather than something a user does to themselves.
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')") // Role changes are a privileged admin action, not self-service
    @Operation(summary = "Update a user's role (admin only)", description = "Role management: promote/correct a user between PASSENGER, DRIVER and ADMIN.")
    public ResponseEntity<UserResponse> updateRole(@PathVariable String id,
                                                     @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    /**
     * Admin-only endpoint that changes a user's account status (ACTIVE/INACTIVE/SUSPENDED).
     * This satisfies the "account status management" requirement in the brief; a
     * non-ACTIVE account is later rejected at login time regardless of a correct password.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')") // Suspending/activating accounts is an admin-only action
    @Operation(summary = "Update a user's account status (admin only)", description = "Account status management: activate, deactivate or suspend an account.")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable String id,
                                                       @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(id, request));
    }
}
