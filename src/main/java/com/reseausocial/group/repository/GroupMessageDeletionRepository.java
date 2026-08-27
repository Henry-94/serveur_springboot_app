package com.reseausocial.group.repository;

import com.reseausocial.group.entity.GroupMessageDeletion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMessageDeletionRepository extends JpaRepository<GroupMessageDeletion, Long> {
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);
}
