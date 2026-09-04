package com.routesync.backend.service;

import com.routesync.backend.dto.bus.BusResponse;
import com.routesync.backend.dto.bus.CreateBusRequest;
import com.routesync.backend.dto.bus.UpdateBusRequest;

import java.util.List;
import java.util.UUID;

public interface BusService {

    /**
     * Create a new bus.
     * ADMIN only.
     */
    BusResponse createBus(CreateBusRequest request);

    /**
     * Get a bus by ID.
     * PASSENGER, DRIVER, ADMIN.
     */
    BusResponse getBusById(UUID id);

    /**
     * Get all active buses.
     * PASSENGER, DRIVER, ADMIN.
     */
    List<BusResponse> getAllActiveBuses();

    /**
     * Update bus information.
     * ADMIN only.
     */
    BusResponse updateBus(UUID id, UpdateBusRequest request);

    /**
     * Soft-deactivate a bus.
     * ADMIN only.
     */
    void deactivateBus(UUID id);
}