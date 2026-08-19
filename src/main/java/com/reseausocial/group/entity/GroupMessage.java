package com.reseausocial.group.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_messages", indexes = @Index(name = "idx_group_messages_group_sent", columnList = "group_id,sent_at"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GroupMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "group_id")
    private StudyGroup group;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_id")
    private User author;
    @Column(nullable = false, length = 2000)
    private String content;
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    @PrePersist void onCreate() { sentAt = LocalDateTime.now(); }
}
