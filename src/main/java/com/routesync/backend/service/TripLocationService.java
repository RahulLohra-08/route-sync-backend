package com.routesync.backend.service;

import com.routesync.backend.dto.trip.location.TripLocationResponse;
import com.routesync.backend.dto.trip.location.UpdateTripLocationRequest;

import java.util.List;
import java.util.UUID;

public interface TripLocationService {

    /**
     * Updates the current GPS location of a driver's active trip
     * and stores the location in trip history.
     */
    TripLocationResponse updateLocation(
            UUID tripId,
            UUID driverUserId,
            UpdateTripLocationRequest request
    );

    /**
     * Returns the complete GPS history of a trip.
     */
    List<TripLocationResponse> getTripLocationHistory(
            UUID tripId
    );

    /**
     * Returns the latest GPS location of a trip.
     */
    TripLocationResponse getLatestLocation(
            UUID tripId
    );

    List<TripLocationResponse> getDriverTripLocationHistory(
            UUID tripId,
            UUID driverUserId
    );

    TripLocationResponse getDriverLatestLocation(
            UUID tripId,
            UUID driverUserId
    );
}