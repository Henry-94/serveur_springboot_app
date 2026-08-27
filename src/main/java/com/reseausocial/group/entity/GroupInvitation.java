package com.reseausocial.group.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_invitations")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GroupInvitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "group_id") private StudyGroup group;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "inviter_id") private User inviter;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invitee_id") private User invitee;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "is_read", nullable = false) private boolean read;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); if (status == null) status = "PENDING"; }
}
