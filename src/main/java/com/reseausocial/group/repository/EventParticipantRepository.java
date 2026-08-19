package com.reseausocial.group.repository;
import com.reseausocial.group.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> { long countByEventId(Long id); boolean existsByEventIdAndUserId(Long eventId, Long userId); }
