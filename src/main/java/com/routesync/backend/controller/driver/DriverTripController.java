package com.routesync.backend.controller.driver;

import com.routesync.backend.dto.trip.TripResponse;
import com.routesync.backend.service.TripService;
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
public class DriverTripController {

    private final TripService tripService;


    @GetMapping
    public ResponseEntity<List<TripResponse>> getMyTrips(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripService.getDriverTrips(userId)
        );
    }


    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getMyTrip(
            @PathVariable UUID tripId,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripService.getDriverTripById(
                        tripId,
                        userId
                )
        );
    }


    @PatchMapping("/{tripId}/start")
    public ResponseEntity<TripResponse> startTrip(
            @PathVariable UUID tripId,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripService.startTripByDriver(
                        tripId,
                        userId
                )
        );
    }


    @PatchMapping("/{tripId}/complete")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable UUID tripId,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripService.completeTripByDriver(
                        tripId,
                        userId
                )
        );
    }


    @PatchMapping("/{tripId}/cancel")
    public ResponseEntity<TripResponse> cancelTrip(
            @PathVariable UUID tripId,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(
                authentication.getName()
        );

        return ResponseEntity.ok(
                tripService.cancelTripByDriver(
                        tripId,
                        userId
                )
        );
    }
}