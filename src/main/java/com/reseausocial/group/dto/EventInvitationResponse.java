package com.reseausocial.group.dto;
import com.reseausocial.group.entity.EventInvitation;
import java.time.LocalDateTime;
public record EventInvitationResponse(Long id, Long eventId, String eventTitle, String inviterName, String status, boolean read, boolean participationRequest, LocalDateTime createdAt) {
    public static EventInvitationResponse of(EventInvitation i) { return new EventInvitationResponse(i.getId(), i.getEvent().getId(), i.getEvent().getTitle(), i.getInviter().getFullName(), i.getStatus(), i.isRead(), i.getEvent().getCreatedBy().getId().equals(i.getInvitee().getId()) && !i.getInviter().getId().equals(i.getInvitee().getId()), i.getCreatedAt()); }
}
