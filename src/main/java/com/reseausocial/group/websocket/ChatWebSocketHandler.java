package com.reseausocial.group.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reseausocial.group.dto.SendMessageRequest;
import com.reseausocial.group.repository.EventParticipantRepository;
import com.reseausocial.group.repository.GroupMemberRepository;
import com.reseausocial.group.repository.UserRepository;
import com.reseausocial.group.service.EventService;
import com.reseausocial.group.service.GroupService;
import com.reseausocial.group.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final JwtUtil jwt;
    private final GroupService groups;
    private final EventService events;
    private final GroupMemberRepository groupMembers;
    private final EventParticipantRepository eventParticipants;
    private final UserRepository users;
    private final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String room = room(session);
        String token = query(session.getUri(), "token");
        try {
            String email = jwt.extractUsername(token);
            Long id = Long.valueOf(room.substring(room.lastIndexOf('/') + 1));
            if (room.startsWith("group/") && !groupMembers.findByGroupIdAndUserId(id, userId(email)).isPresent()) throw new SecurityException();
            if (room.startsWith("event/") && !eventParticipants.existsByEventIdAndUserId(id, userId(email))) throw new SecurityException();
            session.getAttributes().put("email", email);
            session.getAttributes().put("room", room);
            rooms.computeIfAbsent(room, ignored -> ConcurrentHashMap.newKeySet()).add(session);
        } catch (Exception ex) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Accès refusé"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String email = (String) session.getAttributes().get("email");
        String room = (String) session.getAttributes().get("room");
        if (email == null || room == null) return;
        JsonNode body = mapper.readTree(message.getPayload());
        String content = body.path("content").asText("").trim();
        if (content.isBlank() || content.length() > 2000) return;
        Object response = room.startsWith("group/")
                ? groups.send(Long.valueOf(room.substring(6)), new SendMessageRequest(content), email)
                : events.sendMessage(Long.valueOf(room.substring(6)), new SendMessageRequest(content), email);
        String json = mapper.writeValueAsString(response);
        for (WebSocketSession peer : rooms.getOrDefault(room, Set.of())) if (peer.isOpen()) peer.sendMessage(new TextMessage(json));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String room = (String) session.getAttributes().get("room");
        if (room != null) rooms.getOrDefault(room, Set.of()).remove(session);
    }

    private String room(WebSocketSession session) { String path = session.getUri().getPath(); return path.substring(path.indexOf("/chat/") + 6); }
    private String query(URI uri, String key) { if (uri == null || uri.getQuery() == null) return ""; for (String item : uri.getQuery().split("&")) { String[] pair = item.split("=", 2); if (pair.length == 2 && pair[0].equals(key)) return pair[1]; } return ""; }
    private Long userId(String email) { return users.findByEmail(email).orElseThrow().getId(); }
}
