package com.routesync.backend.service;

import com.routesync.backend.dto.stop.CreateStopRequest;
import com.routesync.backend.dto.stop.StopResponse;
import com.routesync.backend.dto.stop.UpdateStopRequest;

import java.util.List;
import java.util.UUID;

public interface StopService {

    /**
     * Create a stop under a specific route.
     */
    StopResponse createStop(
            UUID routeId,
            CreateStopRequest request
    );

    /**
     * Get stop by ID.
     */
    StopResponse getStopById(UUID stopId);

    /**
     * Get all stops of a route.
     *
     * Stops are returned according to stop order.
     */
    List<StopResponse> getStopsByRoute(UUID routeId);

    /**
     * Get all active stops of a route.
     */
    List<StopResponse> getActiveStopsByRoute(UUID routeId);

    /**
     * Update an existing stop.
     */
    StopResponse updateStop(
            UUID stopId,
            UpdateStopRequest request
    );

    /**
     * Deactivate a stop.
     */
    void deactivateStop(UUID stopId);
}