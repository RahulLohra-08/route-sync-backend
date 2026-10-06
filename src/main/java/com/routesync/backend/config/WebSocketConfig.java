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
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void configureMessageBroker( MessageBrokerRegistry registry ) {

        registry.enableSimpleBroker("/topic","/queue")
                .setHeartbeatValue(new long[]{ 10_000, 10_000})
                .setTaskScheduler(heartbeatTaskScheduler());

        registry.setApplicationDestinationPrefixes("/app");

        registry.setUserDestinationPrefix( "/user");
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {

        /*
         * JWT authentication + WebSocket authorization.
         *
         * StompAuthChannelInterceptor already has
         * @Order(HIGHEST_PRECEDENCE + 99).
         *
         * Therefore CONNECT authentication happens before
         * other security authorization processing.
         */
        registration.interceptors(stompAuthChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureWebSocketTransport(
            WebSocketTransportRegistration registration
    ) {

        registration.setMessageSizeLimit(
                128 * 1024
        );

        registration.setSendBufferSizeLimit(
                512 * 1024
        );

        registration.setSendTimeLimit(
                20_000
        );
    }

    @Bean
    public TaskScheduler heartbeatTaskScheduler() {

        ThreadPoolTaskScheduler scheduler =
                new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(1);

        scheduler.setThreadNamePrefix(
                "ws-heartbeat-"
        );

        scheduler.initialize();

        return scheduler;
    }
}