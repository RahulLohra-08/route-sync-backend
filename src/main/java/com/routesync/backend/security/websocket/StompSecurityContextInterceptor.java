package com.routesync.backend.security.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Makes method security (@PreAuthorize) work on STOMP @MessageMapping handlers.
 *
 * WHY THIS EXISTS
 * ---------------
 * StompAuthChannelInterceptor authenticates the CONNECT frame and attaches the
 * Authentication to the STOMP session, but STOMP messages are handled on
 * clientInboundChannel executor threads where SecurityContextHolder is EMPTY.
 * DriverLocationSocketController is annotated with @PreAuthorize("hasRole('DRIVER')"),
 * so every GPS message threw AuthenticationCredentialsNotFoundException, which the
 * catch-all @MessageExceptionHandler turned into
 * INTERNAL_ERROR "Could not process GPS update" - while REST (whose HTTP filter does
 * populate the context) kept working.
 *
 * WHAT IT DOES
 * ------------
 * Right before a message is handled it copies the session's Authentication into the
 * SecurityContext of the handling thread, and clears it afterwards so nothing leaks
 * to the next message on a pooled thread. (Equivalent to Spring Security's
 * SecurityContextChannelInterceptor, without needing the spring-security-messaging
 * dependency.)
 */
@Component
public class StompSecurityContextInterceptor implements ExecutorChannelInterceptor {

    @Override
    public Message<?> beforeHandle(
            Message<?> message,
            MessageChannel channel,
            MessageHandler handler
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getUser() instanceof Authentication authentication) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
        return message;
    }

    @Override
    public void afterMessageHandled(
            Message<?> message,
            MessageChannel channel,
            MessageHandler handler,
            Exception ex
    ) {
        SecurityContextHolder.clearContext();
    }
}
