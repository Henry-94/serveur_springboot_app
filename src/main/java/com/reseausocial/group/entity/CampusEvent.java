package com.reseausocial.group.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campus_events")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CampusEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 180) private String title;
    @Column(length = 1500) private String description;
    @Column(nullable = false) private LocalDateTime startsAt;
    @Column(length = 180) private String location;
    @Column(length = 40) private String category;
    @Lob @Column(name = "image", columnDefinition = "TEXT") private String image;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by") private User createdBy;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
