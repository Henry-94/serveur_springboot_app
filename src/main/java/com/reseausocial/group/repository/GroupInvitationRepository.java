package com.reseausocial.group.repository;
import com.reseausocial.group.entity.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    List<GroupInvitation> findByInviteeIdAndStatusOrderByCreatedAtDesc(Long inviteeId, String status);
    List<GroupInvitation> findByInviteeIdOrderByCreatedAtDesc(Long inviteeId);
    Optional<GroupInvitation> findByIdAndInviteeId(Long id, Long inviteeId);
    boolean existsByGroupIdAndInviteeIdAndStatus(Long groupId, Long inviteeId, String status);
    long countByInviteeIdAndStatus(Long inviteeId, String status);
}
