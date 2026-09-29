package com.ridelink.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned after a successful login.
 * Contains the freshly issued JWT plus a few convenience fields so the caller does not
 * have to decode the token just to know who they are.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;     // The signed JWT; must be sent as "Authorization: Bearer <token>" on later requests
    private String tokenType; // Always "Bearer", included for clients that expect this field explicitly
    private String userId;    // Convenience copy of the userId claim embedded in the token
    private String email;     // Convenience copy of the subject (email) embedded in the token
    private String role;      // Convenience copy of the role claim embedded in the token
}
