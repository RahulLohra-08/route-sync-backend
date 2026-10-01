package com.routesync.backend.config;

import com.routesync.backend.security.websocket.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    /**
     * Configures the STOMP message broker.
     */
    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {

        /*
         * Messages sent to /topic are broadcast
         * to all subscribers of that topic (live bus location,
         * and later route-level aggregates / ETA broadcasts).
         *
         * /queue is used for the per-session, per-user destinations
         * below (e.g. /user/queue/errors) - one driver's failed GPS
         * push must never be seen by anyone else.
         *
         * Heartbeats let the broker and every client detect a dead
         * connection (phone loses signal, app is killed, etc.) within
         * ~10s instead of waiting on TCP timeouts, which matters for
         * a bus that has genuinely gone quiet vs. one whose socket
         * just died.
         */
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10_000, 10_000})
                .setTaskScheduler(heartbeatTaskScheduler());

        /*
         * Application-level messages use /app.
         *
         * GPS ingestion is handled primarily through REST
         * (DriverLocationController), which remains unchanged.
         * DriverLocationSocketController additionally exposes
         * /app/trips/{tripId}/location so an already-connected
         * driver socket can push updates without a second HTTP
         * round trip.
         */
        registry.setApplicationDestinationPrefixes("/app");

        /*
         * Enables user-specific destinations, e.g.
         * "/user/queue/errors", resolved per STOMP session using the
         * Principal set by StompAuthChannelInterceptor.
         */
        registry.setUserDestinationPrefix("/user");
    }


    /**
     * Registers the JWT authentication interceptor on the *inbound*
     * client channel, so every CONNECT/SUBSCRIBE/SEND frame is
     * authenticated before it reaches a broker or @MessageMapping
     * method. See StompAuthChannelInterceptor for why authentication
     * happens here rather than on the HTTP handshake.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }


    /**
     * Configures the WebSocket/STOMP handshake endpoint.
     */
    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureWebSocketTransport(
            WebSocketTransportRegistration registration
    ) {

        registration.setMessageSizeLimit(128 * 1024);
        registration.setSendBufferSizeLimit(512 * 1024);
        registration.setSendTimeLimit(20_000);
    }

    @Bean
    public TaskScheduler heartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}