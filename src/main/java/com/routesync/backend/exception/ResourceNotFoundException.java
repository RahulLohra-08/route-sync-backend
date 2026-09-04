package com.routesync.backend.exception;

/**
 * Jab requested resource database mein nahi milta,
 * tab ye exception throw kiya jayega.
 *
 * Example:
 * User ID se user search kiya, lekin user exist nahi karta.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}