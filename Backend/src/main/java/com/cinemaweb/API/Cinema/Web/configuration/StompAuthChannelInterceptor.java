package com.cinemaweb.API.Cinema.Web.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Xác thực kết nối WebSocket tại frame CONNECT bằng JWT (header Authorization: Bearer ...).
 * Tái sử dụng CustomJwtDecoder để verify chữ ký / hạn / blacklist / loại token.
 * Principal đặt vào session có getName() = userId (subject của JWT) nên
 * convertAndSendToUser(userId, ...) định tuyến đúng.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            if (token == null)
                throw new MessagingException("Missing access token");

            try {
                Jwt jwt = customJwtDecoder.decode(token);
                String userId = jwt.getSubject();
                accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
            } catch (Exception e) {
                throw new MessagingException("Invalid access token");
            }
        }
        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty())
            return null;
        String header = authHeaders.get(0);
        if (header != null && header.startsWith("Bearer "))
            return header.substring(7);
        return null;
    }
}
