package com.reseausocial.group.dto;

import com.reseausocial.group.entity.EventMessage;
import java.time.LocalDateTime;

public record EventMessageResponse(Long id, Long authorId, String authorName, String content, LocalDateTime sentAt) {
    public static EventMessageResponse of(EventMessage message) {
        return new EventMessageResponse(message.getId(), message.getAuthor().getId(), message.getAuthor().getFullName(), message.getContent(), message.getSentAt());
    }
}
