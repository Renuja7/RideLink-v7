package com.ridelink.account.service;

import com.ridelink.account.dto.UpdateRoleRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.dto.UpdateUserRequest;
import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.entity.AccountStatus;
import com.ridelink.account.entity.Role;
import com.ridelink.account.entity.User;
import com.ridelink.account.exception.ResourceNotFoundException;
import com.ridelink.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("1")
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phoneNumber("0771111111")
                .password("encoded")
                .role(Role.DRIVER)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById("1");

        assertEquals("jane@example.com", response.getEmail());
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("99"));
    }

    @Test
    void updateUser_shouldUpdateFields() {
        UpdateUserRequest request = new UpdateUserRequest("Jane Updated", "0779999999");

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser("1", request);

        assertEquals("Jane Updated", response.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateRole_shouldChangeUsersRole() {
        UpdateRoleRequest request = new UpdateRoleRequest(Role.ADMIN);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateRole("1", request);

        assertEquals(Role.ADMIN, response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateRole_shouldThrow_whenUserNotFound() {
        UpdateRoleRequest request = new UpdateRoleRequest(Role.ADMIN);
        when(userRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateRole("99", request));
    }

    @Test
    void updateStatus_shouldChangeAccountStatus() {
        UpdateStatusRequest request = new UpdateStatusRequest(AccountStatus.SUSPENDED);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateStatus("1", request);

        assertEquals(AccountStatus.SUSPENDED, response.getStatus());
    }
}
