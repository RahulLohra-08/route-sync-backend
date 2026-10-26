package com.routesync.backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Observability hook for the STOMP session lifecycle.
 *
 * Today this only logs, but it is the natural place to plug in future
 * features that need to know who is watching what in real time, e.g.:
 *  - counting active passenger subscribers per trip (for scaling
 *    decisions - is anyone even watching this bus?)
 *  - detecting a driver's app going silent mid-trip (an early signal
 *    for delay/route-deviation detection, since a lost connection with
 *    no new GPS points is itself useful information)
 *  - presence indicators in the UI ("3 passengers tracking this bus")
 */
@Component
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.wrap(event.getMessage());

        log.info(
                "WebSocket session connected: user={} session={}",
                event.getUser() != null ? event.getUser().getName() : "anonymous",
                accessor.getSessionId()
        );
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.wrap(event.getMessage());

        log.info(
                "WebSocket subscribe: user={} destination={} session={}",
                event.getUser() != null ? event.getUser().getName() : "anonymous",
                accessor.getDestination(),
                accessor.getSessionId()
        );
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {

        log.info(
                "WebSocket session disconnected: user={} session={} closeStatus={}",
                event.getUser() != null ? event.getUser().getName() : "anonymous",
                event.getSessionId(),
                event.getCloseStatus()
        );
    }
}
