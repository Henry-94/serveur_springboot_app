package com.reseausocial.group.dto;

import com.reseausocial.group.entity.EventMessage;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public record EventMessageResponse(Long id, Long authorId, String authorName, String content, LocalDateTime sentAt, LocalDateTime deliveredAt, LocalDateTime readAt, String messageType, String fileName, String contentType, String attachmentData, Map<String,Integer> reactions) {
    public static EventMessageResponse of(EventMessage message) {
        return new EventMessageResponse(message.getId(), message.getAuthor().getId(), message.getAuthor().getFullName(), message.getContent(), message.getSentAt(), message.getDeliveredAt(), message.getReadAt(), message.getMessageType(), message.getFileName(), message.getContentType(), message.getAttachmentData(), parse(message.getReactions()));
    }
    public static Map<String,Integer> parse(String value) { Map<String,Integer> result = new LinkedHashMap<>(); if (value == null || value.isBlank()) return result; for (String item : value.split(";")) { String[] pair = item.split("=", 2); if (pair.length == 2) try { result.put(pair[0], Integer.parseInt(pair[1])); } catch (NumberFormatException ignored) {} } return result; }
    public static String serialize(Map<String,Integer> values) { StringBuilder result = new StringBuilder(); values.forEach((key, count) -> { if (result.length() > 0) result.append(';'); result.append(key).append('=').append(count); }); return result.toString(); }
}
