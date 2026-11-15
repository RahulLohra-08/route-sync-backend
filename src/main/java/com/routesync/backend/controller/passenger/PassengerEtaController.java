
package com.routesync.backend.controller.passenger;

import com.routesync.backend.dto.trip.location.TripEtaResponse;
import com.routesync.backend.service.TripEtaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/passenger/trips")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PASSENGER')")
public class PassengerEtaController {

    private final TripEtaService tripEtaService;

    // Henglish: Passenger trip aur target stop bhejkar ETA maang sakta hai.
    @GetMapping("/{tripId}/eta")
    public ResponseEntity<TripEtaResponse> getEta(
            @PathVariable UUID tripId,
            @RequestParam UUID stopId
    ) {
        return ResponseEntity.ok(
                tripEtaService.calculateEta(tripId, stopId)
        );
    }
}
