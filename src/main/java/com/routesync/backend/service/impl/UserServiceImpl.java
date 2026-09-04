package com.routesync.backend.service.impl;

import com.routesync.backend.dto.CreateUserRequest;
import com.routesync.backend.dto.UpdateUserRequest;
import com.routesync.backend.dto.UserResponse;
import com.routesync.backend.entity.AuthProvider;
import com.routesync.backend.entity.User;
import com.routesync.backend.entity.UserRole;
import com.routesync.backend.exception.DuplicateResourceException;
import com.routesync.backend.exception.ResourceNotFoundException;
import com.routesync.backend.repository.UserRepository;
import com.routesync.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * UserService ka actual business logic yahan implement hoga.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    /**
     * New user create karta hai.
     */
    @Override
    public UserResponse createUser(CreateUserRequest request) {

        /*
         * Email duplicate check.
         */
        if (request.getEmail() != null
                && userRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }


        /*
         * Phone number duplicate check.
         */
        if (request.getPhoneNumber() != null
                && userRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {

            throw new DuplicateResourceException(
                    "Phone number already registered"
            );
        }


        /*
         * Request DTO ko User entity mein convert kar rahe hain.
         */
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profileImage(request.getProfileImage())
                .authProvider(AuthProvider.OTP)
                .role(UserRole.PASSENGER)
                .active(true)
                .build();


        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }


    /**
     * UUID ke basis par user find karta hai.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        return mapToResponse(user);
    }


    /**
     * Saare users return karta hai.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /**
     * Email ke basis par user find karta hai.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return mapToResponse(user);
    }


    /**
     * Phone number ke basis par user find karta hai.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByPhoneNumber(
            String phoneNumber
    ) {

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with phone: "
                                        + phoneNumber
                        )
                );

        return mapToResponse(user);
    }


    /**
     * Google ID ke basis par user find karta hai.
     *
     * Future Google OAuth2 implementation mein useful hoga.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByGoogleId(String googleId) {

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with Google ID"
                        )
                );

        return mapToResponse(user);
    }


    /**
     * Existing user update karta hai.
     */
    @Override
    public UserResponse updateUser(
            UUID id,
            UpdateUserRequest request
    ) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );


        /*
         * Name update.
         */
        if (request.getFullName() != null) {

            existingUser.setFullName(
                    request.getFullName()
            );
        }


        /*
         * Email update.
         *
         * Agar new email kisi aur user ka hai
         * to duplicate error throw karenge.
         */
        if (request.getEmail() != null
                && !request.getEmail().equals(existingUser.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {

                throw new DuplicateResourceException(
                        "Email already registered"
                );
            }

            existingUser.setEmail(request.getEmail());
        }


        /*
         * Phone number update.
         */
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber()
                .equals(existingUser.getPhoneNumber())) {

            if (userRepository.existsByPhoneNumber(
                    request.getPhoneNumber())) {

                throw new DuplicateResourceException(
                        "Phone number already registered"
                );
            }

            existingUser.setPhoneNumber(
                    request.getPhoneNumber()
            );
        }


        /*
         * Profile image update.
         */
        if (request.getProfileImage() != null) {

            existingUser.setProfileImage(
                    request.getProfileImage()
            );
        }


        User updatedUser = userRepository.save(existingUser);

        return mapToResponse(updatedUser);
    }


    /**
     * User ko deactivate karta hai.
     *
     * Database se permanently delete nahi karenge.
     */
    @Override
    public void deactivateUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        user.setActive(false);

        userRepository.save(user);
    }


    /**
     * User entity ko API response DTO mein convert karta hai.
     */
    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .authProvider(user.getAuthProvider())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}