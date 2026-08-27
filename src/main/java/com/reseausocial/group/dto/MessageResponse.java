package com.reseausocial.group.dto;
import com.reseausocial.group.entity.GroupMessage;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
public record MessageResponse(Long id, Long authorId, String authorName, String content, LocalDateTime sentAt, LocalDateTime deliveredAt, LocalDateTime readAt, String messageType, String fileName, String contentType, String attachmentData, Map<String,Integer> reactions) {
    public static MessageResponse of(GroupMessage m) { return new MessageResponse(m.getId(), m.getAuthor().getId(), m.getAuthor().getFullName(), m.getContent(), m.getSentAt(), m.getDeliveredAt(), m.getReadAt(), m.getMessageType(), m.getFileName(), m.getContentType(), m.getAttachmentData(), parse(m.getReactions())); }
    public static Map<String,Integer> parse(String value) { Map<String,Integer> result = new LinkedHashMap<>(); if (value == null || value.isBlank()) return result; for (String item : value.split(";")) { String[] pair = item.split("=", 2); if (pair.length == 2) try { result.put(pair[0], Integer.parseInt(pair[1])); } catch (NumberFormatException ignored) {} } return result; }
    public static String serialize(Map<String,Integer> values) { StringBuilder result = new StringBuilder(); values.forEach((key, count) -> { if (result.length() > 0) result.append(';'); result.append(key).append('=').append(count); }); return result.toString(); }
}
