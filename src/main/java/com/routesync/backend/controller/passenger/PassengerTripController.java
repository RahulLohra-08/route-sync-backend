
package com.routesync.backend.controller.passenger;

import com.routesync.backend.dto.trip.TripResponse;
import com.routesync.backend.dto.trip.location.TripTrackingSnapshotResponse;
import com.routesync.backend.service.TripService;
import com.routesync.backend.service.TripTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/passenger/trips")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PASSENGER')")
public class PassengerTripController {

    private final TripService tripService;
    private final TripTrackingService tripTrackingService;

    // Passenger ko available trips ki list return karta hai.
    @GetMapping
    public ResponseEntity<List<TripResponse>> getAvailableTrips() {
        return ResponseEntity.ok(
                tripService.getAvailableTrips()
        );
    }

    // Specific trip ki details return karta hai.
    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable UUID tripId
    ) {
        return ResponseEntity.ok(
                tripService.getTripById(tripId)
        );
    }

    // Kisi particular route ki available trips return karta hai.
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TripResponse>> getTripsByRoute(
            @PathVariable UUID routeId
    ) {
        return ResponseEntity.ok(
                tripService.getAvailableTripsByRoute(routeId)
        );
    }

    // Passenger ko trip ka latest GPS snapshot aur location freshness deta hai.
    /**
     * Returns current trip information and the latest GPS snapshot.
     *
     * Henglish:
     * Passenger screen open karte hi current location milegi.
     * Next updates existing WebSocket connection se aayenge.
     */
    @GetMapping("/{tripId}/tracking")
    public ResponseEntity<TripTrackingSnapshotResponse> getTrackingSnapshot(
            @PathVariable UUID tripId
    ) {
        return ResponseEntity.ok(
                tripTrackingService.getTrackingSnapshot(tripId)
        );
    }
}
