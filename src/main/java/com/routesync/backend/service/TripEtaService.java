
package com.routesync.backend.service;

import com.routesync.backend.dto.trip.location.TripEtaResponse;

import java.util.UUID;

public interface TripEtaService {

    TripEtaResponse calculateEta(UUID tripId, UUID stopId);

}
