package com.routesync.backend.websocket.event;

import com.routesync.backend.dto.trip.location.TripLocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transaction commit hone ke baad live location broadcast karta hai.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TripLocationWebSocketListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleLocationUpdate(
            TripLocationUpdatedEvent event
    ) {

        TripLocationResponse location =
                event.location();

        String tripDestination =
                "/topic/trips/"
                        + location.tripId()
                        + "/location";

        /*
         * Henglish:
         *
         * Ye public live-trip topic hai.
         *
         * Passenger/Admin/authorized Driver jo bhi is topic
         * ko subscribe karega, usko new GPS point milega.
         */
        messagingTemplate.convertAndSend(
                tripDestination,
                event.broadcastMessage()
        );

        /*
         * Driver ko private acknowledgement.
         *
         * /user destination automatically current driver's
         * authenticated Principal ke according resolve hota hai.
         */
        messagingTemplate.convertAndSendToUser(
                event.driverUserId().toString(),
                "/queue/location-ack",
                location
        );

        log.debug(
                "Live location broadcasted: trip={} lat={} lon={}",
                location.tripId(),
                location.latitude(),
                location.longitude()
        );
    }
}