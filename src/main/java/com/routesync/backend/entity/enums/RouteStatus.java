package com.routesync.backend.entity.enums;

/**
 * Defines the operational status of a route.
 *
 * ACTIVE:
 * Route is currently available for operations.
 *
 * INACTIVE:
 * Route is temporarily unavailable.
 *
 * SUSPENDED:
 * Route has been suspended due to operational
 * or administrative reasons.
 */
public enum RouteStatus {

    ACTIVE,
    INACTIVE,
    SUSPENDED
}