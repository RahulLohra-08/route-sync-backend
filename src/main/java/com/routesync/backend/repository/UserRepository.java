package com.routesync.backend.repository;

import com.routesync.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserRepository database ke saath communication handle karega.
 *
 * JpaRepository ki wajah se hamein basic CRUD ke liye
 * manually SQL likhne ki zarurat nahi hai.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Email ke basis par user find karega.
     *
     * Google login ke case mein useful hoga.
     */
    Optional<User> findByEmail(String email);


    /**
     * Phone number ke basis par user find karega.
     *
     * OTP login ke case mein useful hoga.
     */
    Optional<User> findByPhoneNumber(String phoneNumber);


    /**
     * Google ke unique ID ke basis par user find karega.
     *
     * Google OAuth login mein ye important hoga.
     */
    Optional<User> findByGoogleId(String googleId);


    /**
     * Check karega ki email already registered hai ya nahi.
     */
    boolean existsByEmail(String email);


    /**
     * Check karega ki phone number already registered hai ya nahi.
     */
    boolean existsByPhoneNumber(String phoneNumber);


    /**
     * Check karega ki Google account already linked hai ya nahi.
     */
    boolean existsByGoogleId(String googleId);

//    List<User> findAllByIsActiveTrue();
}