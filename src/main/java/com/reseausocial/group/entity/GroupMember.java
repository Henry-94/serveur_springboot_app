package com.reseausocial.group.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GroupMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "group_id")
    private StudyGroup group;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, length = 20)
    private String role;
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    @PrePersist void onCreate() { joinedAt = LocalDateTime.now(); if (role == null) role = "MEMBER"; }
}
