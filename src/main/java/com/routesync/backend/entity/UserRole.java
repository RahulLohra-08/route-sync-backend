package com.routesync.backend.entity;

/**
 * User ka role define karta hai.
 * Hamein future mein Role-Based Access Control (RBAC) ke liye
 * ye enum kaafi useful rahega.
 */
public enum UserRole {

    // Normal passenger jo bus track karega
    PASSENGER,

    // Bus driver jo apni live location share karega
    DRIVER,

    // Transport system ko manage karne wala admin
    ADMIN
}