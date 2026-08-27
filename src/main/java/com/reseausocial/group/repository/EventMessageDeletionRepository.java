package com.reseausocial.group.repository;

import com.reseausocial.group.entity.EventMessageDeletion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMessageDeletionRepository extends JpaRepository<EventMessageDeletion, Long> {
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);
}
