package com.routesync.backend.controller;

import com.routesync.backend.dto.CreateUserRequest;
import com.routesync.backend.dto.UpdateUserRequest;
import com.routesync.backend.dto.UserResponse;
import com.routesync.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
/**
 * User related REST APIs.
 *
 * Controller ka kaam:
 * - HTTP request receive karna
 * - Validation trigger karna
 * - Service ko call karna
 * - HTTP response return karna
 *
 * Business logic Controller mein nahi rakhenge.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * New user create API.
     *
     * POST /api/v1/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {

        UserResponse response = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /**
     * Saare users get karne ki API.
     *
     * GET /api/v1/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    /**
     * Currently authenticated user ka profile return karta hai.
     *
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                userService.getUserById(userId)
        );
    }

    /**
     * Currently authenticated user ka profile update karta hai.
     *
     * PUT /api/v1/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                userService.updateUser(userId, request)
        );
    }

    /**
     * Currently authenticated user ka account deactivate karta hai.
     *
     * DELETE /api/v1/users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateCurrentUser(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        userService.deactivateUser(userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * ID ke basis par user find karta hai.
     *
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }


    /**
     * Email ke basis par user find karta hai.
     *
     * GET /api/v1/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email
    ) {

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }


    /**
     * Phone number ke basis par user find karta hai.
     *
     * GET /api/v1/users/phone/{phoneNumber}
     */
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<UserResponse> getUserByPhoneNumber(
            @PathVariable String phoneNumber
    ) {

        return ResponseEntity.ok(
                userService.getUserByPhoneNumber(phoneNumber)
        );
    }


    /**
     * Existing user update API.
     *
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }


    /**
     * User account deactivate API.
     *
     * DELETE /api/v1/users/{id}
     *
     * Actual database delete nahi hoga.
     * Sirf active = false hoga.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable UUID id
    ) {

        userService.deactivateUser(id);

        return ResponseEntity.noContent().build();
    }
}