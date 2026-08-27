package com.reseausocial.group.controller;

import com.reseausocial.group.dto.*;
import com.reseausocial.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/groups") @RequiredArgsConstructor
public class GroupController {
    private final GroupService service;
    @GetMapping public List<GroupResponse> list(@AuthenticationPrincipal UserDetails u) { return service.list(u.getUsername()); }
    @PostMapping public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest r, @AuthenticationPrincipal UserDetails u) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r, u.getUsername())); }
    @PostMapping("/{id}/join") public GroupResponse join(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.join(id, u.getUsername()); }
    @PostMapping("/{id}/invite") public InvitationResponse invite(@PathVariable Long id, @Valid @RequestBody InviteMemberRequest r, @AuthenticationPrincipal UserDetails u) { return service.invite(id, r.email(), u.getUsername()); }
    @GetMapping("/{id}/invite-candidates") public List<UserResponse> inviteCandidates(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.inviteCandidates(id, u.getUsername()); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { service.delete(id, u.getUsername()); return ResponseEntity.noContent().build(); }
    @GetMapping("/{id}/members") public List<MemberResponse> members(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.members(id, u.getUsername()); }
    @DeleteMapping("/{id}/members/me") public ResponseEntity<Void> leave(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { service.leave(id, u.getUsername()); return ResponseEntity.noContent().build(); }
    @DeleteMapping("/{id}/members/{userId}") public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal UserDetails u) { service.removeMember(id, userId, u.getUsername()); return ResponseEntity.noContent().build(); }
    @GetMapping("/{id}/messages") public List<MessageResponse> messages(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.messages(id, u.getUsername()); }
    @PostMapping("/{id}/messages/read") public ResponseEntity<Void> markMessagesRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { service.markMessagesRead(id, u.getUsername()); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/messages") public ResponseEntity<MessageResponse> send(@PathVariable Long id, @Valid @RequestBody SendMessageRequest r, @AuthenticationPrincipal UserDetails u) { return ResponseEntity.status(HttpStatus.CREATED).body(service.send(id, r, u.getUsername())); }
    @PostMapping("/{id}/messages/attachment") public ResponseEntity<MessageResponse> attachment(@PathVariable Long id, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal UserDetails u) throws java.io.IOException { validateAttachment(file); return ResponseEntity.status(HttpStatus.CREATED).body(service.sendAttachment(id, file, u.getUsername())); }
    @PostMapping("/{id}/messages/{messageId}/reaction") public MessageResponse reaction(@PathVariable Long id, @PathVariable Long messageId, @Valid @RequestBody ReactionRequest r, @AuthenticationPrincipal UserDetails u) { return service.react(id, messageId, r.emoji(), u.getUsername()); }
    @DeleteMapping("/{id}/messages/{messageId}") public ResponseEntity<Void> deleteMessage(@PathVariable Long id, @PathVariable Long messageId, @RequestParam(defaultValue = "me") String scope, @AuthenticationPrincipal UserDetails u) { service.deleteMessage(id, messageId, scope, u.getUsername()); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/image") public GroupResponse image(@PathVariable Long id, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal UserDetails u) throws java.io.IOException {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier image invalide.");
        if (file.getSize() > 5 * 1024 * 1024) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image trop volumineuse.");
        return service.updateImage(id, u.getUsername(), "data:" + file.getContentType() + ";base64," + Base64.getEncoder().encodeToString(file.getBytes()));
    }
    private void validateAttachment(MultipartFile file) { if (file.isEmpty() || file.getContentType() == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier invalide."); if (file.getSize() > 5 * 1024 * 1024) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Fichier trop volumineux (5 Mo maximum)."); }
}
