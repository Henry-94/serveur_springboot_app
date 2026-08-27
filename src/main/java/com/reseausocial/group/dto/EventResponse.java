package com.reseausocial.group.dto;
import com.reseausocial.group.entity.CampusEvent;
import java.time.LocalDateTime;
public record EventResponse(Long id, String title, String description, LocalDateTime startsAt, String location, String category, String image, Long creatorId, LocalDateTime createdAt, long attendees, boolean registered, boolean participationPending, boolean canParticipate, String lastMessage, LocalDateTime lastMessageAt, Long lastMessageAuthorId) {
    public static EventResponse of(CampusEvent e, long attendees, boolean registered, boolean participationPending, boolean canParticipate) { return of(e, attendees, registered, participationPending, canParticipate, null); }
    public static EventResponse of(CampusEvent e, long attendees, boolean registered, boolean participationPending, boolean canParticipate, com.reseausocial.group.entity.EventMessage last) { return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getStartsAt(), e.getLocation(), e.getCategory(), e.getImage(), e.getCreatedBy().getId(), e.getCreatedAt(), attendees, registered, participationPending, canParticipate, last == null ? null : last.getContent(), last == null ? null : last.getSentAt(), last == null ? null : last.getAuthor().getId()); }
}
