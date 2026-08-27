package com.reseausocial.group.repository;
import com.reseausocial.group.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> { long countByEventId(Long id); boolean existsByEventIdAndUserId(Long eventId, Long userId); List<EventParticipant> findByEventIdOrderByRegisteredAtAsc(Long eventId); void deleteByEventIdAndUserId(Long eventId, Long userId); }
