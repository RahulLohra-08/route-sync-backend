package com.routesync.backend.security;

import com.routesync.backend.entity.User;
import com.routesync.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Spring Security ko User ki information provide karta hai.
 *
 * JWT ke andar hum User ka UUID subject ke roop mein rakhenge.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UUID userId;

        try {
            userId = UUID.fromString(username);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException(
                    "Invalid user ID in authentication token"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with ID: " + userId
                        )
                );

        if (!user.getActive()) {
            throw new UsernameNotFoundException(
                    "User account is inactive"
            );
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password("")
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}