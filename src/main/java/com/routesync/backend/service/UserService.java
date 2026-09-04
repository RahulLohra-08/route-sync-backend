package com.routesync.backend.service;

import com.routesync.backend.dto.CreateUserRequest;
import com.routesync.backend.dto.UpdateUserRequest;
import com.routesync.backend.dto.UserResponse;

import java.util.List;
import java.util.UUID;

/**
 * User related business operations yahan define honge.
 *
 * Flow:
 *
 * Controller
 *     ↓
 * Service
 *     ↓
 * Repository
 *     ↓
 * PostgreSQL
 */

public interface UserService {

    /**
     * New user create karega.
     */
    UserResponse createUser(CreateUserRequest request);


    /**
     * UUID ke basis par user find karega.
     */
    UserResponse getUserById(UUID id);


    /**
     * Saare users return karega.
     */
    List<UserResponse> getAllUsers();


    /**
     * Email ke basis par user find karega.
     */
    UserResponse getUserByEmail(String email);

    /**
     * Phone number ke basis par user find karega.
     */
    UserResponse getUserByPhoneNumber(String phoneNumber);


    /**
     * Google ID ke basis par user find karega.
     *
     * Ye method mainly future OAuth2 implementation
     * mein use hoga.
     */
    UserResponse getUserByGoogleId(String googleId);


    /**
     * Existing user update karega.
     */
    UserResponse updateUser(UUID id, UpdateUserRequest request);


    /**
     * User account deactivate karega.
     *
     * Hard delete ke bajay soft delete approach use karenge.
     */
    void deactivateUser(UUID id);
}