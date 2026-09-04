package com.routesync.backend.dto;

import com.routesync.backend.entity.AuthProvider;
import com.routesync.backend.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API response ke liye UserResponse DTO.
 *
 * Important:
 * User entity directly client ko return nahi karenge.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String profileImage;

    private AuthProvider authProvider;

    private UserRole role;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}