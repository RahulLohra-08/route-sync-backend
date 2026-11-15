package com.routesync.backend.controller.admin;

import com.routesync.backend.dto.trip.CreateTripRequest;
import com.routesync.backend.dto.trip.TripResponse;
import com.routesync.backend.dto.trip.UpdateTripRequest;
import com.routesync.backend.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/trips")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTripController {

    private final TripService tripService;

    /**
     * Create a new Trip.
     *
     * Henglish:
     * Sirf ADMIN new Trip create kar sakta hai.
     *
     * Trip create karte time:
     * - Route validate hoga
     * - Bus validate hoga
     * - Driver validate hoga
     * - Schedule overlap check hoga
     * - Bus capacity automatically Trip mein snapshot hogi
     */
    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody CreateTripRequest request
    ) {

        TripResponse response = tripService.createTrip(request);

        return ResponseEntity
                .status(201)
                .body(response);
    }

    /**
     * Get all active Trips.
     */
    @GetMapping
    public ResponseEntity<List<TripResponse>> getAllTrips() {

        return ResponseEntity.ok(
                tripService.getAllActiveTrips()
        );
    }

    /**
     * Get Trip by ID.
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable UUID tripId
    ) {

        return ResponseEntity.ok(
                tripService.getTripById(tripId)
        );
    }

    /**
     * Get Trips by Route.
     *
     * Example:
     * GET /api/v1/admin/trips/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TripResponse>> getTripsByRoute(
            @PathVariable UUID routeId
    ) {

        return ResponseEntity.ok(
                tripService.getTripsByRoute(routeId)
        );
    }

    /**
     * Get Trips by Bus.
     */
    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<TripResponse>> getTripsByBus(
            @PathVariable UUID busId
    ) {

        return ResponseEntity.ok(
                tripService.getTripsByBus(busId)
        );
    }

    /**
     * Get Trips by Driver.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripResponse>> getTripsByDriver(
            @PathVariable UUID driverId
    ) {

        return ResponseEntity.ok(
                tripService.getTripsByDriver(driverId)
        );
    }

    /**
     * Update Trip.
     *
     * Henglish:
     * Route, Bus, Driver aur schedule update kar sakte hain.
     * Service layer overlap aur lifecycle rules validate karegi.
     */
    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable UUID tripId,
            @Valid @RequestBody UpdateTripRequest request
    ) {

        return ResponseEntity.ok(
                tripService.updateTrip(tripId, request)
        );
    }

    /**
     * Deactivate Trip.
     *
     * Henglish:
     * Database se Trip delete nahi hoga.
     * Sirf active=false hoga.
     */
    @PatchMapping("/{tripId}/deactivate")
    public ResponseEntity<Void> deactivateTrip(
            @PathVariable UUID tripId
    ) {

        tripService.deactivateTrip(tripId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Start Trip.
     *
     * SCHEDULED/BOARDING → IN_PROGRESS
     */
    @PatchMapping("/{tripId}/start")
    public ResponseEntity<TripResponse> startTrip(
            @PathVariable UUID tripId
    ) {

        return ResponseEntity.ok(
                tripService.startTrip(tripId)
        );
    }

    /**
     * Complete Trip.
     *
     * IN_PROGRESS → COMPLETED
     */
    @PatchMapping("/{tripId}/complete")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable UUID tripId
    ) {

        return ResponseEntity.ok(
                tripService.completeTrip(tripId)
        );
    }

    /**
     * Cancel Trip.
     */
    @PatchMapping("/{tripId}/cancel")
    public ResponseEntity<TripResponse> cancelTrip(
            @PathVariable UUID tripId
    ) {

        return ResponseEntity.ok(
                tripService.cancelTrip(tripId)
        );
    }
}