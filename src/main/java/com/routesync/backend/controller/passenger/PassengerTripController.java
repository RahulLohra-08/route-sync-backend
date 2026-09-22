package com.routesync.backend.controller.passenger;

import com.routesync.backend.dto.trip.TripResponse;
import com.routesync.backend.service.TripService;
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


    @GetMapping
    public ResponseEntity<List<TripResponse>> getAvailableTrips() {

        return ResponseEntity.ok(
                tripService.getAvailableTrips()
        );
    }


    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(
            @PathVariable UUID tripId
    ) {

        return ResponseEntity.ok(
                tripService.getTripById(tripId)
        );
    }


    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TripResponse>> getTripsByRoute(
            @PathVariable UUID routeId
    ) {

        return ResponseEntity.ok(
                tripService.getAvailableTripsByRoute(routeId)
        );
    }
}