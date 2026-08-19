package com.reseausocial.group.dto;
import com.reseausocial.group.entity.StudyGroup;
import java.time.LocalDateTime;
public record GroupResponse(Long id, String name, String description, Long creatorId, String creatorName, long members, LocalDateTime createdAt, String image, String lastMessage, LocalDateTime lastMessageAt, Long lastMessageAuthorId) {
    public static GroupResponse of(StudyGroup g, long members) { return new GroupResponse(g.getId(), g.getName(), g.getDescription(), g.getCreatedBy().getId(), g.getCreatedBy().getFullName(), members, g.getCreatedAt(), g.getImage(), null, null, null); }
    public static GroupResponse of(StudyGroup g, long members, com.reseausocial.group.entity.GroupMessage last) { return new GroupResponse(g.getId(), g.getName(), g.getDescription(), g.getCreatedBy().getId(), g.getCreatedBy().getFullName(), members, g.getCreatedAt(), g.getImage(), last == null ? null : last.getContent(), last == null ? null : last.getSentAt(), last == null ? null : last.getAuthor().getId()); }
}
