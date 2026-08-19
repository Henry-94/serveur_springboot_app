package com.reseausocial.group.service;

import com.reseausocial.group.dto.*;
import com.reseausocial.group.entity.*;
import com.reseausocial.group.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class EventService {
    private final CampusEventRepository events;
    private final EventParticipantRepository participants;
    private final EventMessageRepository messages;
    private final EventInvitationRepository invitations;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<EventResponse> list(String email) {
        User current = user(email);
        return events.findAllByOrderByStartsAtAsc().stream()
                .map(event -> EventResponse.of(event, participants.countByEventId(event.getId()),
                        participants.existsByEventIdAndUserId(event.getId(), current.getId()),
                        invitations.existsByEventIdAndInviterIdAndInviteeIdAndStatus(event.getId(), current.getId(), event.getCreatedBy().getId(), "PENDING"),
                        messages.findTop1ByEventIdOrderBySentAtDesc(event.getId()).orElse(null)))
                .toList();
    }

    @Transactional
    public EventResponse create(CreateEventRequest request, String email) {
        if (request.startsAt().isBefore(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de l'événement doit être future.");
        User creator = user(email);
        CampusEvent event = events.saveAndFlush(CampusEvent.builder()
                .title(request.title().trim())
                .description(request.description() == null || request.description().isBlank() ? null : request.description().trim())
                .startsAt(request.startsAt()).location(request.location()).category(request.category()).image(request.image()).createdBy(creator).build());
        participants.save(EventParticipant.builder().event(event).user(creator).build());
        return EventResponse.of(event, 1, true, false);
    }

    @Transactional
    public EventResponse participate(Long id, String email) {
        CampusEvent event = event(id); User current = user(email);
        if (participants.existsByEventIdAndUserId(id, current.getId())) return EventResponse.of(event, participants.countByEventId(id), true, false);
        if (!invitations.existsByEventIdAndInviterIdAndInviteeIdAndStatus(id, current.getId(), event.getCreatedBy().getId(), "PENDING")) {
            invitations.save(EventInvitation.builder().event(event).inviter(current).invitee(event.getCreatedBy()).status("PENDING").build());
        }
        return EventResponse.of(event, participants.countByEventId(id), false, true);
    }

    @Transactional(readOnly = true)
    public List<EventMessageResponse> messages(Long id, String email) {
        requireParticipant(id, email);
        return messages.findTop100ByEventIdOrderBySentAtDesc(id).stream().sorted(java.util.Comparator.comparing(EventMessage::getSentAt)).map(EventMessageResponse::of).toList();
    }

    @Transactional
    public EventMessageResponse sendMessage(Long id, SendMessageRequest request, String email) {
        requireParticipant(id, email);
        return EventMessageResponse.of(messages.save(EventMessage.builder().event(event(id)).author(user(email)).content(request.content().trim()).build()));
    }

    private void requireParticipant(Long id, String email) {
        if (!participants.existsByEventIdAndUserId(id, user(email).getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Participez à l'événement pour accéder à la discussion.");
    }
    private CampusEvent event(Long id) { return events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable.")); }
    private User user(String email) { return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable.")); }
}
