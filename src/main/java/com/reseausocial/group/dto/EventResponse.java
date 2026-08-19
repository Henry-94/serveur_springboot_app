package com.reseausocial.group.dto;
import com.reseausocial.group.entity.CampusEvent;
import java.time.LocalDateTime;
public record EventResponse(Long id, String title, String description, LocalDateTime startsAt, String location, String category, String image, Long creatorId, long attendees, boolean registered, boolean participationPending, String lastMessage, LocalDateTime lastMessageAt, Long lastMessageAuthorId) {
    public static EventResponse of(CampusEvent e, long attendees, boolean registered, boolean participationPending) { return of(e, attendees, registered, participationPending, null); }
    public static EventResponse of(CampusEvent e, long attendees, boolean registered, boolean participationPending, com.reseausocial.group.entity.EventMessage last) { return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getStartsAt(), e.getLocation(), e.getCategory(), e.getImage(), e.getCreatedBy().getId(), attendees, registered, participationPending, last == null ? null : last.getContent(), last == null ? null : last.getSentAt(), last == null ? null : last.getAuthor().getId()); }
}
