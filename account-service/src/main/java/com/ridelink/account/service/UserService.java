package com.ridelink.account.service;

import com.ridelink.account.dto.UpdateRoleRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.dto.UpdateUserRequest;
import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.entity.User;
import com.ridelink.account.exception.ResourceNotFoundException;
import com.ridelink.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for viewing and managing existing user accounts: profile lookups,
 * profile edits, and the two admin-only management operations (role changes and
 * account-status changes).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** Looks up a single user by id, or throws ResourceNotFoundException (-> 404) if absent. */
    public UserResponse getUserById(String id) {
        User user = findUserOrThrow(id);
        return toResponse(user);
    }

    /** Returns every registered user's public profile; only reachable by ADMIN callers (enforced in the controller). */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Updates a user's self-editable profile fields (full name, phone number).
     * Intentionally does not touch email, password, role or status.
     */
    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    /**
     * Role management: allows an administrator to change a user's role
     * (e.g. promoting/correcting PASSENGER, DRIVER or ADMIN) after registration.
     */
    @Transactional
    public UserResponse updateRole(String id, UpdateRoleRequest request) {
        User user = findUserOrThrow(id);
        user.setRole(request.getRole());
        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    /**
     * Account status management: allows an administrator to activate,
     * deactivate or suspend a user's account independently of profile updates.
     */
    @Transactional
    public UserResponse updateStatus(String id, UpdateStatusRequest request) {
        User user = findUserOrThrow(id);
        user.setStatus(request.getStatus());
        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    // Shared lookup helper: fetches a user by id or throws a 404-mapped exception,
    // used by every method above so the "not found" behaviour stays consistent.
    private User findUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
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
