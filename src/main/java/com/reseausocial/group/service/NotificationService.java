package com.reseausocial.group.service;

import com.reseausocial.group.dto.*;
import com.reseausocial.group.entity.*;
import com.reseausocial.group.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service @RequiredArgsConstructor
public class NotificationService {
    private final GroupInvitationRepository invitations;
    private final GroupMemberRepository members;
    private final UserRepository users;
    private final EventInvitationRepository eventInvitations;
    private final EventParticipantRepository participants;
    @Transactional(readOnly = true) public List<InvitationResponse> pending(String email) { return invitations.findByInviteeIdAndStatusOrderByCreatedAtDesc(user(email).getId(), "PENDING").stream().map(InvitationResponse::of).toList(); }
    @Transactional(readOnly = true) public List<InvitationResponse> allGroups(String email) { return invitations.findByInviteeIdOrderByCreatedAtDesc(user(email).getId()).stream().map(InvitationResponse::of).toList(); }
    @Transactional(readOnly = true) public NotificationFeedResponse all(String email) { return new NotificationFeedResponse(allGroups(email), allEvents(email)); }
    @Transactional public void readGroup(Long id, String email) { GroupInvitation i = invitation(id, user(email).getId()); i.setRead(true); invitations.save(i); }
    @Transactional public void deleteGroup(Long id, String email) { invitations.delete(invitation(id, user(email).getId())); }
    @Transactional public InvitationResponse accept(Long id, String email) { User user = user(email); GroupInvitation i = invitation(id, user.getId()); if (!members.findByGroupIdAndUserId(i.getGroup().getId(), user.getId()).isPresent()) members.save(GroupMember.builder().group(i.getGroup()).user(user).role("MEMBER").build()); i.setStatus("ACCEPTED"); i.setRead(true); return InvitationResponse.of(invitations.save(i)); }
    @Transactional public InvitationResponse reject(Long id, String email) { GroupInvitation i = invitation(id, user(email).getId()); i.setStatus("REJECTED"); i.setRead(true); return InvitationResponse.of(invitations.save(i)); }
    @Transactional(readOnly = true) public List<EventInvitationResponse> eventPending(String email) { return List.of(); }
    @Transactional(readOnly = true) public List<EventInvitationResponse> allEvents(String email) { return eventInvitations.findByInviteeIdOrderByCreatedAtDesc(user(email).getId()).stream().filter(i -> !i.getEvent().getCreatedBy().getId().equals(i.getInviter().getId())).map(EventInvitationResponse::of).toList(); }
    @Transactional public void readEvent(Long id, String email) { EventInvitation i = eventInvitation(id, user(email).getId()); i.setRead(true); eventInvitations.save(i); }
    @Transactional public void deleteEvent(Long id, String email) { eventInvitations.delete(eventInvitation(id, user(email).getId())); }
    @Transactional(readOnly = true) public List<EventInvitationResponse> eventRequests(String email) {
        User current = user(email);
        return eventInvitations.findByInviteeIdAndStatusOrderByCreatedAtDesc(current.getId(), "PENDING").stream()
                .filter(i -> i.getEvent().getCreatedBy().getId().equals(current.getId()) && !i.getInviter().getId().equals(current.getId()))
                .map(EventInvitationResponse::of).toList();
    }
    @Transactional(readOnly = true) public NotificationCountResponse count(String email) { Long id = user(email).getId(); long groups = invitations.countByInviteeIdAndStatus(id, "PENDING"); long events = eventInvitations.findByInviteeIdAndStatusOrderByCreatedAtDesc(id, "PENDING").stream().filter(i -> i.getEvent().getCreatedBy().getId().equals(id) && !i.getInviter().getId().equals(id)).count(); return new NotificationCountResponse(groups, events, groups + events); }
    @Transactional public EventInvitationResponse acceptEvent(Long id, String email) { User current = user(email); EventInvitation i = eventInvitation(id, current.getId()); if (!participants.existsByEventIdAndUserId(i.getEvent().getId(), current.getId())) participants.save(EventParticipant.builder().event(i.getEvent()).user(current).build()); i.setStatus("ACCEPTED"); i.setRead(true); return EventInvitationResponse.of(eventInvitations.save(i)); }
    @Transactional public EventInvitationResponse rejectEvent(Long id, String email) { EventInvitation i = eventInvitation(id, user(email).getId()); i.setStatus("REJECTED"); i.setRead(true); return EventInvitationResponse.of(eventInvitations.save(i)); }
    @Transactional public EventInvitationResponse acceptEventRequest(Long id, String email) { User creator = user(email); EventInvitation i = eventRequest(id, creator.getId()); if (!participants.existsByEventIdAndUserId(i.getEvent().getId(), i.getInviter().getId())) participants.save(EventParticipant.builder().event(i.getEvent()).user(i.getInviter()).build()); i.setStatus("ACCEPTED"); i.setRead(true); return EventInvitationResponse.of(eventInvitations.save(i)); }
    @Transactional public EventInvitationResponse rejectEventRequest(Long id, String email) { EventInvitation i = eventRequest(id, user(email).getId()); i.setStatus("REJECTED"); i.setRead(true); return EventInvitationResponse.of(eventInvitations.save(i)); }
    private GroupInvitation invitation(Long id, Long userId) { return invitations.findByIdAndInviteeId(id, userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation introuvable.")); }
    private EventInvitation eventInvitation(Long id, Long userId) { return eventInvitations.findByIdAndInviteeId(id, userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation événement introuvable.")); }
    private EventInvitation eventRequest(Long id, Long creatorId) { return eventInvitations.findByIdAndInviteeId(id, creatorId).filter(i -> i.getEvent().getCreatedBy().getId().equals(creatorId) && !i.getInviter().getId().equals(creatorId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande de participation introuvable.")); }
    private User user(String email) { return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable.")); }
}
