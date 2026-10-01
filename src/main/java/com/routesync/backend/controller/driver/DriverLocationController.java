package com.routesync.backend.controller.driver;

import com.routesync.backend.dto.trip.location.TripLocationResponse;
import com.routesync.backend.dto.trip.location.UpdateTripLocationRequest;
import com.routesync.backend.service.TripLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver/trips")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
public class DriverLocationController {

    private final TripLocationService tripLocationService;


    // =========================================================
    // UPDATE CURRENT LOCATION
    // =========================================================

//    Patch: /api/v1/driver/trips/{tripId}/location
    @PatchMapping("/{tripId}/location")
    public ResponseEntity<TripLocationResponse> updateLocation(
            @PathVariable UUID tripId,
            @Valid @RequestBody UpdateTripLocationRequest request,
            Authentication authentication
    ) {

        UUID driverUserId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripLocationService.updateLocation(
                        tripId,
                        driverUserId,
                        request
                )
        );
    }


    // =========================================================
    // LOCATION HISTORY
    // =========================================================

    @GetMapping("/{tripId}/location/history")
    public ResponseEntity<List<TripLocationResponse>> getLocationHistory(
            @PathVariable UUID tripId,
            Authentication authentication
    ) {

        UUID driverUserId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripLocationService.getDriverTripLocationHistory(
                        tripId,
                        driverUserId
                )
        );
    }


    // =========================================================
    // LATEST LOCATION
    // =========================================================

    @GetMapping("/{tripId}/location/latest")
    public ResponseEntity<TripLocationResponse> getLatestLocation(
            @PathVariable UUID tripId,
            Authentication authentication
    ) {

        UUID driverUserId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripLocationService.getDriverLatestLocation(
                        tripId,
                        driverUserId
                )
        );
    }
    
}