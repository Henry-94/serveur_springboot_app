package com.reseausocial.group.repository;

import com.reseausocial.group.entity.EventMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventMessageRepository extends JpaRepository<EventMessage, Long> {
    List<EventMessage> findByEventIdOrderBySentAtAsc(Long eventId);
    List<EventMessage> findTop100ByEventIdOrderBySentAtDesc(Long eventId);
    java.util.Optional<EventMessage> findTop1ByEventIdOrderBySentAtDesc(Long eventId);
}
