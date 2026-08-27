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
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service @RequiredArgsConstructor
public class EventService {
    private final CampusEventRepository events;
    private final EventParticipantRepository participants;
    private final EventMessageRepository messages;
    private final EventInvitationRepository invitations;
    private final UserRepository users;
    private final EventMessageDeletionRepository deletions;

    @Transactional(readOnly = true)
    public List<EventResponse> list(String email) {
        User current = user(email);
        return events.findAllByOrderByStartsAtAsc().stream()
                .map(event -> {
                    boolean registered = participants.existsByEventIdAndUserId(event.getId(), current.getId());
                    boolean invited = invitations.existsByEventIdAndInviterIdAndInviteeIdAndStatus(event.getId(), event.getCreatedBy().getId(), current.getId(), "PENDING");
                    boolean requestPending = invitations.existsByEventIdAndInviterIdAndInviteeIdAndStatus(event.getId(), current.getId(), event.getCreatedBy().getId(), "PENDING");
                    return EventResponse.of(event, participants.countByEventId(event.getId()), registered, requestPending, registered || invited, messages.findTop1ByEventIdOrderBySentAtDesc(event.getId()).orElse(null));
                })
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
        return EventResponse.of(event, 1, true, false, true);
    }

    @Transactional
    public EventResponse participate(Long id, String email) {
        CampusEvent event = event(id); User current = user(email);
        if (participants.existsByEventIdAndUserId(id, current.getId())) return EventResponse.of(event, participants.countByEventId(id), true, false, true);
        EventInvitation invitation = invitations.findByEventIdAndInviterIdAndInviteeIdAndStatus(id, event.getCreatedBy().getId(), current.getId(), "PENDING").orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous devez être invité par l’administrateur pour participer à cet événement."));
        participants.save(EventParticipant.builder().event(event).user(current).build());
        invitation.setStatus("ACCEPTED"); invitation.setRead(true); invitations.save(invitation);
        return EventResponse.of(event, participants.countByEventId(id), true, false, true);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> inviteCandidates(Long id, String email) {
        requireCreator(id, email);
        return users.findAll().stream().filter(candidate -> !participants.existsByEventIdAndUserId(id, candidate.getId())).map(UserResponse::fromEntity).toList();
    }

    @Transactional
    public EventInvitationResponse invite(Long id, String invitedEmail, String inviterEmail) {
        CampusEvent event = event(id); User inviter = user(inviterEmail);
        if (!event.getCreatedBy().getId().equals(inviter.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l’administrateur peut inviter un participant.");
        User invited = user(invitedEmail.trim().toLowerCase());
        if (participants.existsByEventIdAndUserId(id, invited.getId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet utilisateur participe déjà à l’événement.");
        if (invitations.existsByEventIdAndInviterIdAndInviteeIdAndStatus(id, inviter.getId(), invited.getId(), "PENDING")) throw new ResponseStatusException(HttpStatus.CONFLICT, "Une invitation est déjà en attente.");
        return EventInvitationResponse.of(invitations.save(EventInvitation.builder().event(event).inviter(inviter).invitee(invited).status("PENDING").build()));
    }

    @Transactional(readOnly = true)
    public List<EventMessageResponse> messages(Long id, String email) {
        requireParticipant(id, email);
        Long userId = user(email).getId();
        return messages.findTop100ByEventIdOrderBySentAtDesc(id).stream().filter(message -> !deletions.existsByMessageIdAndUserId(message.getId(), userId)).sorted(java.util.Comparator.comparing(EventMessage::getSentAt)).map(EventMessageResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> participants(Long id, String email) { requireParticipant(id, email); return participants.findByEventIdOrderByRegisteredAtAsc(id).stream().map(item -> MemberResponse.of(item.getUser(), item.getEvent().getCreatedBy().getId().equals(item.getUser().getId()) ? "CREATOR" : "MEMBER")).toList(); }

    @Transactional
    public void leave(Long id, String email) { User current = user(email); if (event(id).getCreatedBy().getId().equals(current.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le créateur ne peut pas quitter son événement."); participants.deleteByEventIdAndUserId(id, current.getId()); }

    @Transactional
    public void removeParticipant(Long id, Long participantId, String email) {
        CampusEvent event = event(id); User current = user(email);
        if (!event.getCreatedBy().getId().equals(current.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l’administrateur peut supprimer un participant.");
        if (event.getCreatedBy().getId().equals(participantId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L’administrateur ne peut pas être supprimé de l’événement.");
        if (!participants.existsByEventIdAndUserId(id, participantId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant introuvable dans cet événement.");
        participants.deleteByEventIdAndUserId(id, participantId);
    }

    @Transactional
    public void markMessagesRead(Long id, String email) {
        requireParticipant(id, email); messages.markUnreadAsRead(id, user(email).getId(), LocalDateTime.now());
    }

    @Transactional
    public EventMessageResponse sendMessage(Long id, SendMessageRequest request, String email) {
        requireParticipant(id, email);
        return EventMessageResponse.of(messages.save(EventMessage.builder().event(event(id)).author(user(email)).content(request.content().trim()).messageType("TEXT").build()));
    }
    @Transactional
    public EventMessageResponse sendAttachment(Long id, MultipartFile file, String email) throws java.io.IOException {
        requireParticipant(id, email);
        return EventMessageResponse.of(messages.save(EventMessage.builder().event(event(id)).author(user(email)).content(file.getOriginalFilename() == null ? "Fichier" : file.getOriginalFilename()).messageType(file.getContentType() != null && file.getContentType().startsWith("audio/") ? "AUDIO" : "FILE").fileName(file.getOriginalFilename()).contentType(file.getContentType()).attachmentData(Base64.getEncoder().encodeToString(file.getBytes())).build()));
    }
    @Transactional
    public EventMessageResponse markDelivered(Long id, String email) {
        EventMessage message = messages.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable."));
        if (message.getDeliveredAt() == null) message.setDeliveredAt(LocalDateTime.now());
        return EventMessageResponse.of(messages.save(message));
    }
    @Transactional
    public EventMessageResponse react(Long eventId, Long messageId, String emoji, String email) {
        requireParticipant(eventId, email);
        EventMessage message = messages.findById(messageId).filter(item -> item.getEvent().getId().equals(eventId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable."));
        if (!Map.of("👍", true, "❤️", true, "😂", true, "😮", true, "😢", true, "🙏", true, "👏", true).containsKey(emoji)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Réaction non supportée.");
        Long userId = user(email).getId();
        Map<String,String> byUser = reactionUsers(message.getReactionUsers());
        String previous = byUser.put(String.valueOf(userId), emoji);
        Map<String,Integer> reactions = EventMessageResponse.parse(message.getReactions());
        if (previous != null && !previous.equals(emoji)) decrement(reactions, previous);
        if (previous == null || !previous.equals(emoji)) reactions.put(emoji, reactions.getOrDefault(emoji, 0) + 1);
        message.setReactionUsers(serializeReactionUsers(byUser));
        message.setReactions(EventMessageResponse.serialize(reactions));
        return EventMessageResponse.of(messages.save(message));
    }
    private void decrement(Map<String,Integer> reactions, String emoji) { int count = reactions.getOrDefault(emoji, 0) - 1; if (count <= 0) reactions.remove(emoji); else reactions.put(emoji, count); }
    private Map<String,String> reactionUsers(String value) { Map<String,String> result = new java.util.LinkedHashMap<>(); if (value == null || value.isBlank()) return result; for (String item : value.split(";")) { String[] pair = item.split(":", 2); if (pair.length == 2) result.put(pair[0], pair[1]); } return result; }
    private String serializeReactionUsers(Map<String,String> values) { StringBuilder result = new StringBuilder(); values.forEach((id, emoji) -> { if (result.length() > 0) result.append(';'); result.append(id).append(':').append(emoji); }); return result.toString(); }
    @Transactional
    public void deleteMessage(Long eventId, Long messageId, String scope, String email) {
        requireParticipant(eventId, email); User current = user(email);
        EventMessage message = messages.findById(messageId).filter(item -> item.getEvent().getId().equals(eventId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable."));
        if (!"me".equals(scope) && !"everyone".equals(scope)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Portée de suppression invalide.");
        if ("me".equals(scope)) { if (!deletions.existsByMessageIdAndUserId(messageId, current.getId())) deletions.save(EventMessageDeletion.builder().message(message).user(current).build()); return; }
        if (!message.getAuthor().getId().equals(current.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l’auteur peut supprimer ce message pour tout le monde.");
        message.setContent("Ce message a été supprimé"); message.setMessageType("DELETED"); message.setFileName(null); message.setContentType(null); message.setAttachmentData(null); message.setReactions(null); messages.save(message);
    }

    private void requireParticipant(Long id, String email) {
        if (!participants.existsByEventIdAndUserId(id, user(email).getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Participez à l'événement pour accéder à la discussion.");
    }
    private void requireCreator(Long id, String email) { if (!event(id).getCreatedBy().getId().equals(user(email).getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l’administrateur peut inviter un participant."); }
    private CampusEvent event(Long id) { return events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable.")); }
    private User user(String email) { return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable.")); }
}
