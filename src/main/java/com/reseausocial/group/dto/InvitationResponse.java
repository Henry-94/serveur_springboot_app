package com.reseausocial.group.dto;
import com.reseausocial.group.entity.GroupInvitation;
import java.time.LocalDateTime;
public record InvitationResponse(Long id, Long groupId, String groupName, String inviterName, String status, boolean read, LocalDateTime createdAt) {
    public static InvitationResponse of(GroupInvitation i) { return new InvitationResponse(i.getId(), i.getGroup().getId(), i.getGroup().getName(), i.getInviter().getFullName(), i.getStatus(), i.isRead(), i.getCreatedAt()); }
}
