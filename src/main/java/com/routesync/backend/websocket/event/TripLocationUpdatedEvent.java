package com.routesync.backend.websocket.event;

import com.routesync.backend.dto.trip.TripResponse;
import com.routesync.backend.dto.trip.location.TripLocationResponse;
import com.routesync.backend.websocket.TripLocationWebSocketMessage;

import java.util.UUID;

/**
 * Database transaction successfully update hone ke baad
 * WebSocket layer ko notify karne ke liye event.
 *
 * Henglish:
 *
 * Service directly WebSocket nahi bhejega.
 * Pehle DB transaction complete hogi.
 * Uske baad event listener broadcast karega.
 */
public record TripLocationUpdatedEvent(

        UUID driverUserId,

        TripLocationResponse location,

        TripLocationWebSocketMessage broadcastMessage
) {
}