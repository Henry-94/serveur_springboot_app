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
    @Column(name = "sent_at", nullable = false) private LocalDateTime sentAt;
    @PrePersist void onCreate() { sentAt = LocalDateTime.now(); }
}
