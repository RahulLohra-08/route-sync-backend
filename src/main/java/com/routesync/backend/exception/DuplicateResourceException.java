package com.routesync.backend.exception;

/**
 * Jab koi resource already exist karta hai,
 * tab ye exception use hoga.
 *
 * Example:
 * Same email se dobara account create karna.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}