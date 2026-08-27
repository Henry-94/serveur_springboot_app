package com.reseausocial.group.controller;
import com.reseausocial.group.dto.*;
import com.reseausocial.group.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController @RequestMapping("/api/events") @RequiredArgsConstructor
public class EventController {
    private final EventService service;
    @GetMapping public List<EventResponse> list(@AuthenticationPrincipal UserDetails u) { return service.list(u.getUsername()); }
    @PostMapping public EventResponse create(@Valid @RequestBody CreateEventRequest r, @AuthenticationPrincipal UserDetails u) { return service.create(r, u.getUsername()); }
    @PostMapping("/{id}/participate") public EventResponse participate(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.participate(id, u.getUsername()); }
    @GetMapping("/{id}/invite-candidates") public List<UserResponse> inviteCandidates(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.inviteCandidates(id, u.getUsername()); }
    @PostMapping("/{id}/invite") public EventInvitationResponse invite(@PathVariable Long id, @Valid @RequestBody InviteMemberRequest r, @AuthenticationPrincipal UserDetails u) { return service.invite(id, r.email(), u.getUsername()); }
    @GetMapping("/{id}/participants") public List<MemberResponse> participants(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.participants(id, u.getUsername()); }
    @DeleteMapping("/{id}/participants/me") public ResponseEntity<Void> leave(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { service.leave(id, u.getUsername()); return ResponseEntity.noContent().build(); }
    @DeleteMapping("/{id}/participants/{userId}") public ResponseEntity<Void> removeParticipant(@PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal UserDetails u) { service.removeParticipant(id, userId, u.getUsername()); return ResponseEntity.noContent().build(); }
    @GetMapping("/{id}/messages") public List<EventMessageResponse> messages(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.messages(id, u.getUsername()); }
    @PostMapping("/{id}/messages/read") public ResponseEntity<Void> markMessagesRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { service.markMessagesRead(id, u.getUsername()); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/messages") public EventMessageResponse sendMessage(@PathVariable Long id, @Valid @RequestBody SendMessageRequest r, @AuthenticationPrincipal UserDetails u) { return service.sendMessage(id, r, u.getUsername()); }
    @PostMapping("/{id}/messages/attachment") public ResponseEntity<EventMessageResponse> attachment(@PathVariable Long id, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal UserDetails u) throws java.io.IOException { validateAttachment(file); return ResponseEntity.status(HttpStatus.CREATED).body(service.sendAttachment(id, file, u.getUsername())); }
    @PostMapping("/{id}/messages/{messageId}/reaction") public EventMessageResponse reaction(@PathVariable Long id, @PathVariable Long messageId, @Valid @RequestBody ReactionRequest r, @AuthenticationPrincipal UserDetails u) { return service.react(id, messageId, r.emoji(), u.getUsername()); }
    @DeleteMapping("/{id}/messages/{messageId}") public ResponseEntity<Void> deleteMessage(@PathVariable Long id, @PathVariable Long messageId, @RequestParam(defaultValue = "me") String scope, @AuthenticationPrincipal UserDetails u) { service.deleteMessage(id, messageId, scope, u.getUsername()); return ResponseEntity.noContent().build(); }
    private void validateAttachment(MultipartFile file) { if (file.isEmpty() || file.getContentType() == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier invalide."); if (file.getSize() > 5 * 1024 * 1024) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Fichier trop volumineux (5 Mo maximum)."); }
}
