package com.routesync.backend.controller;

import com.routesync.backend.dto.bus.BusResponse;
import com.routesync.backend.dto.bus.CreateBusRequest;
import com.routesync.backend.dto.bus.UpdateBusRequest;
import com.routesync.backend.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    /**
     * Create a new bus.
     *
     * ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusResponse> createBus(
            @Valid @RequestBody CreateBusRequest request
    ) {

        BusResponse response = busService.createBus(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get all active buses.
     *
     * PASSENGER, DRIVER and ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<BusResponse>> getAllActiveBuses() {

        return ResponseEntity.ok(
                busService.getAllActiveBuses()
        );
    }

    /**
     * Get a single active bus.
     *
     * PASSENGER, DRIVER and ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<BusResponse> getBusById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                busService.getBusById(id)
        );
    }

    /**
     * Update bus information.
     *
     * ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusResponse> updateBus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBusRequest request
    ) {

        return ResponseEntity.ok(
                busService.updateBus(id, request)
        );
    }

    /**
     * Soft-deactivate a bus.
     *
     * ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateBus(
            @PathVariable UUID id
    ) {

        busService.deactivateBus(id);

        return ResponseEntity.noContent().build();
    }
}