package com.reseausocial.group.repository;

import com.reseausocial.group.entity.EventMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EventMessageRepository extends JpaRepository<EventMessage, Long> {
    List<EventMessage> findByEventIdOrderBySentAtAsc(Long eventId);
    List<EventMessage> findTop100ByEventIdOrderBySentAtDesc(Long eventId);
    java.util.Optional<EventMessage> findTop1ByEventIdOrderBySentAtDesc(Long eventId);
    @Modifying
    @Query("update EventMessage m set m.readAt = :readAt where m.event.id = :eventId and m.author.id <> :userId and m.readAt is null")
    int markUnreadAsRead(@Param("eventId") Long eventId, @Param("userId") Long userId, @Param("readAt") java.time.LocalDateTime readAt);
}
