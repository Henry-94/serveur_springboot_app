package com.reseausocial.group.dto;
import com.reseausocial.group.entity.GroupMessage;
import java.time.LocalDateTime;
public record MessageResponse(Long id, Long authorId, String authorName, String content, LocalDateTime sentAt) { public static MessageResponse of(GroupMessage m) { return new MessageResponse(m.getId(), m.getAuthor().getId(), m.getAuthor().getFullName(), m.getContent(), m.getSentAt()); } }
