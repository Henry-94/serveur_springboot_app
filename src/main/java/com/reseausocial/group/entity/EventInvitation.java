package com.reseausocial.group.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "event_invitations") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EventInvitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private CampusEvent event;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private User inviter;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private User invitee;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(name = "is_read", nullable = false) private boolean read;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); if (status == null) status = "PENDING"; }
}
