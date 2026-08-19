package com.reseausocial.group.repository;
import com.reseausocial.group.entity.EventInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EventInvitationRepository extends JpaRepository<EventInvitation, Long> {
    boolean existsByEventIdAndInviteeIdAndStatus(Long eventId, Long inviteeId, String status);
    boolean existsByEventIdAndInviterIdAndInviteeIdAndStatus(Long eventId, Long inviterId, Long inviteeId, String status);
    List<EventInvitation> findByInviteeIdAndStatusOrderByCreatedAtDesc(Long inviteeId, String status);
    List<EventInvitation> findByInviteeIdOrderByCreatedAtDesc(Long inviteeId);
    List<EventInvitation> findByInviterIdAndStatusOrderByCreatedAtDesc(Long inviterId, String status);
    Optional<EventInvitation> findByIdAndInviteeId(Long id, Long inviteeId);
    Optional<EventInvitation> findByIdAndInviterId(Long id, Long inviterId);
    long countByInviteeIdAndStatus(Long inviteeId, String status);
}
