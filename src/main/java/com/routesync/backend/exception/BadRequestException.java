package com.routesync.backend.exception;

/**
 * Jab client ki request valid nahi hoti,
 * tab ye exception use kiya jayega.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}