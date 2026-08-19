package com.reseausocial.group.controller;
import com.reseausocial.group.dto.*;
import com.reseausocial.group.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

@RestController @RequestMapping("/api/events") @RequiredArgsConstructor
public class EventController {
    private final EventService service;
    @GetMapping public List<EventResponse> list(@AuthenticationPrincipal UserDetails u) { return service.list(u.getUsername()); }
    @PostMapping public EventResponse create(@Valid @RequestBody CreateEventRequest r, @AuthenticationPrincipal UserDetails u) { return service.create(r, u.getUsername()); }
    @PostMapping("/{id}/participate") public EventResponse participate(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.participate(id, u.getUsername()); }
    @GetMapping("/{id}/messages") public List<EventMessageResponse> messages(@PathVariable Long id, @AuthenticationPrincipal UserDetails u) { return service.messages(id, u.getUsername()); }
    @PostMapping("/{id}/messages") public EventMessageResponse sendMessage(@PathVariable Long id, @Valid @RequestBody SendMessageRequest r, @AuthenticationPrincipal UserDetails u) { return service.sendMessage(id, r, u.getUsername()); }
}
