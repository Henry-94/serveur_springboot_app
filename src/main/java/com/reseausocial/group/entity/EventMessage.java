package com.reseausocial.group.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_messages", indexes = @Index(name = "idx_event_messages_event_sent", columnList = "event_id,sent_at"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EventMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "event_id") private CampusEvent event;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_id") private User author;
    @Column(nullable = false, length = 2000) private String content;
    @Column(name = "message_type", length = 20) private String messageType;
    @Column(name = "file_name", length = 255) private String fileName;
    @Column(name = "content_type", length = 120) private String contentType;
    @Lob @Column(name = "attachment_data", columnDefinition = "TEXT") private String attachmentData;
    @Column(name = "reactions", length = 1000) private String reactions;
    @Column(name = "reaction_users", length = 4000) private String reactionUsers;
    @Column(name = "sent_at", nullable = false) private LocalDateTime sentAt;
    @Column(name = "read_at") private LocalDateTime readAt;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
    @PrePersist void onCreate() { sentAt = LocalDateTime.now(); }
}
