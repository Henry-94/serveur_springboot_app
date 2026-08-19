package com.reseausocial.group.controller;

import com.reseausocial.group.dto.*;
import com.reseausocial.group.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;
    @GetMapping("/invitations") public List<InvitationResponse> invitations(@AuthenticationPrincipal UserDetails u) { return service.pending(u.getUsername()); }
    @GetMapping("/all") public NotificationFeedResponse all(@AuthenticationPrincipal UserDetails u) { return service.all(u.getUsername()); }
    @PostMapping("/invitations/{id}/accept") public InvitationResponse accept(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.accept(id, u.getUsername()); }
    @PostMapping("/invitations/{id}/reject") public InvitationResponse reject(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.reject(id, u.getUsername()); }
    @PostMapping("/invitations/{id}/read") public void readGroup(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { service.readGroup(id, u.getUsername()); }
    @GetMapping("/event-invitations") public List<EventInvitationResponse> eventInvitations(@AuthenticationPrincipal UserDetails u) { return service.eventPending(u.getUsername()); }
    @GetMapping("/event-requests") public List<EventInvitationResponse> eventRequests(@AuthenticationPrincipal UserDetails u) { return service.eventRequests(u.getUsername()); }
    @GetMapping("/count") public NotificationCountResponse count(@AuthenticationPrincipal UserDetails u) { return service.count(u.getUsername()); }
    @PostMapping("/event-invitations/{id}/accept") public EventInvitationResponse acceptEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.acceptEvent(id, u.getUsername()); }
    @PostMapping("/event-invitations/{id}/reject") public EventInvitationResponse rejectEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.rejectEvent(id, u.getUsername()); }
    @PostMapping("/event-invitations/{id}/read") public void readEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { service.readEvent(id, u.getUsername()); }
    @PostMapping("/event-requests/{id}/accept") public EventInvitationResponse acceptEventRequest(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.acceptEventRequest(id, u.getUsername()); }
    @PostMapping("/event-requests/{id}/reject") public EventInvitationResponse rejectEventRequest(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.rejectEventRequest(id, u.getUsername()); }
}
