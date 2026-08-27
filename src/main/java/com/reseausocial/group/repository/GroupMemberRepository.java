package com.reseausocial.group.repository;
import com.reseausocial.group.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupMember> findByGroupIdOrderByJoinedAtAsc(Long groupId);
    long countByGroupId(Long groupId);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}
