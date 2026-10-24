
package com.routesync.backend.service;

import com.routesync.backend.dto.trip.location.TripTrackingSnapshotResponse;

import java.util.UUID;

public interface TripTrackingService {

    TripTrackingSnapshotResponse getTrackingSnapshot(UUID tripId);

}
