package com.iescamp.studyflow_api.config;

import com.iescamp.studyflow_api.security.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtUtils jwtUtils;
    private final WebSocketHandler roomSignalingHandler;

    public WebSocketConfig(JwtUtils jwtUtils, WebSocketHandler roomSignalingHandler) {
        this.jwtUtils = jwtUtils;
        this.roomSignalingHandler = roomSignalingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomSignalingHandler, "/ws/rooms")
                .addInterceptors(authInterceptor())
                .setAllowedOrigins("*"); // media is peer-to-peer; signaling origin locked down via CORS policy token
    }

    /**
     * JWT gate on the handshake: invalid/expired tokens never get a socket.
     * Extracts room code + user email into socket attributes.
     */
    private HandshakeInterceptor authInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) {
                if (!(request instanceof ServletServerHttpRequest servlet)) return false;
                String token = servlet.getServletRequest().getParameter("token");
                String room = servlet.getServletRequest().getParameter("room");
                Claims claims = jwtUtils.validateToken(token == null ? "" : token);
                if (claims == null || room == null || room.isBlank()) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
                attributes.put("email", claims.getSubject());
                attributes.put("room", room.trim());
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
            }
        };
    }
}
