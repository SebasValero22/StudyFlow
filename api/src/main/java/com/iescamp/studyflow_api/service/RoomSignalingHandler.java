package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.security.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebRTC signaling relay for peer-to-peer study rooms.
 *
 * Peers authenticate on the handshake with ?token=<jwt>. The server NEVER
 * touches media - it only relays SDP offers/answers and ICE candidates
 * between members of the same room code. Rooms vanish when the last peer
 * leaves: zero persistence, zero cost.
 */
@Component
public class RoomSignalingHandler extends TextWebSocketHandler {

    private final JwtUtils jwtUtils;

    /** roomCode -> connected sessions */
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public RoomSignalingHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String code = roomOf(session);
        String email = emailOf(session);
        if (code == null || email == null) {
            closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
        }
        // Session is registered only when it sends a "join" message.
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Map<String, Object> msg = parse(message.getPayload());
        if (msg == null) return;
        String type = String.valueOf(msg.get("type"));

        switch (type) {
            case "join" -> {
                String code = roomOf(session);
                if (code == null || !code.equals(String.valueOf(msg.get("room")))) return;
                rooms.computeIfAbsent(code, k -> new CopyOnWriteArraySet<>()).add(session);
                // Tell the newcomer how many peers are already here (excluding self handled client-side)
                broadcast(code, session, Map.of(
                        "type", "peer-joined",
                        "peers", rooms.get(code).size() - 1));
            }
            case "offer", "answer", "ice" -> {
                String code = roomOf(session);
                if (code != null && rooms.getOrDefault(code, Set.of()).contains(session)) {
                    broadcast(code, session, Map.of(
                            "type", type,
                            "data", msg.get("data")));
                }
            }
            case "leave" -> leave(session);
            default -> { /* unknown types ignored */ }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        leave(session);
    }

    private void leave(WebSocketSession session) {
        String code = roomOf(session);
        if (code == null) return;
        Set<WebSocketSession> room = rooms.get(code);
        if (room != null && room.remove(session) && room.isEmpty()) {
            rooms.remove(code, room);
        }
    }

    private void broadcast(String code, WebSocketSession from, Map<String, Object> payload) {
        String senderEmail = emailOf(from);
        Map<String, Object> enriched = new java.util.HashMap<>(payload);
        enriched.put("from", senderEmail);
        TextMessage out = new TextMessage(toJson(enriched));

        for (WebSocketSession peer : rooms.getOrDefault(code, Set.of())) {
            if (peer.getId().equals(from.getId())) continue;
            try {
                synchronized (peer) {
                    if (peer.isOpen()) peer.sendMessage(out);
                }
            } catch (IOException e) {
                closeQuietly(peer, CloseStatus.SERVER_ERROR);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> parse(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof String s) sb.append('"').append(s.replace("\"", "\\\"")).append('"');
            else if (v instanceof Map<?, ?> inner) sb.append(toJson((Map<String, Object>) inner));
            else sb.append(v);
        }
        return sb.append('}').toString();
    }

    private String roomOf(WebSocketSession session) {
        return attr(session, "room");
    }

    private String emailOf(WebSocketSession session) {
        return attr(session, "email");
    }

    private String attr(WebSocketSession session, String key) {
        Object value = session.getAttributes().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ignored) {
        }
    }
}
