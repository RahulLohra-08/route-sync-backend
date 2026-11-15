package com.routesync.backend.entity.enums;

public enum TripStatus {

    /**
     * Trip has been created and is waiting for departure.
     */
    SCHEDULED,

    /**
     * Bus is preparing for departure / passengers are boarding.
     */
    BOARDING,

    /**
     * Trip is currently running.
     */
    IN_PROGRESS,

    /**
     * Trip has successfully completed.
     */
    COMPLETED,

    /**
     * Trip was cancelled before completion.
     */
    CANCELLED
}