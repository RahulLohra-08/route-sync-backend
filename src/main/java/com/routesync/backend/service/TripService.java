package com.routesync.backend.service;

import com.routesync.backend.dto.trip.CreateTripRequest;
import com.routesync.backend.dto.trip.TripResponse;
import com.routesync.backend.dto.trip.UpdateTripRequest;

import java.util.List;
import java.util.UUID;

public interface TripService {

    // =========================
    // ADMIN / COMMON OPERATIONS
    // =========================

    /**
     * Create a new trip.
     *
     * Route, Bus and Driver must exist and be active.
     * Bus/Driver schedule conflicts will be checked.
     */
    TripResponse createTrip(CreateTripRequest request);

    /**
     * Get an active trip by ID.
     */
    TripResponse getTripById(UUID tripId);

    /**
     * Get all active trips.
     */
    List<TripResponse> getAllActiveTrips();

    /**
     * Get active trips operating on a specific route.
     */
    List<TripResponse> getTripsByRoute(UUID routeId);

    /**
     * Get active trips assigned to a specific bus.
     */
    List<TripResponse> getTripsByBus(UUID busId);

    /**
     * Get active trips assigned to a specific driver.
     */
    List<TripResponse> getTripsByDriver(UUID driverId);

    /**
     * Update trip configuration.
     *
     * Schedule, route, bus and driver changes are validated
     * against business rules.
     */
    TripResponse updateTrip(
            UUID tripId,
            UpdateTripRequest request
    );

    /**
     * Soft-delete / deactivate a trip.
     */
    void deactivateTrip(UUID tripId);

    /**
     * Start a scheduled/boarding trip.
     */
    TripResponse startTrip(UUID tripId);

    /**
     * Mark an in-progress trip as completed.
     */
    TripResponse completeTrip(UUID tripId);

    /**
     * Cancel a trip.
     */
    TripResponse cancelTrip(UUID tripId);

    // =========================
    // DRIVER OPERATIONS
    // =========================

    List<TripResponse> getDriverTrips(UUID driverUserId);

    TripResponse getDriverTripById(
            UUID tripId,
            UUID driverUserId
    );

    TripResponse startTripByDriver(
            UUID tripId,
            UUID driverUserId
    );

    TripResponse completeTripByDriver(
            UUID tripId,
            UUID driverUserId
    );

    TripResponse cancelTripByDriver(
            UUID tripId,
            UUID driverUserId
    );

//    Passenger Related service
    List<TripResponse> getAvailableTrips();

    List<TripResponse> getAvailableTripsByRoute(UUID routeId);
}