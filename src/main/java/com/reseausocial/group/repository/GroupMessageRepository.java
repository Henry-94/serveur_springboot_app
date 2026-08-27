package com.reseausocial.group.repository;
import com.reseausocial.group.entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupIdOrderBySentAtAsc(Long groupId);
    List<GroupMessage> findTop100ByGroupIdOrderBySentAtDesc(Long groupId);
    java.util.Optional<GroupMessage> findTop1ByGroupIdOrderBySentAtDesc(Long groupId);
    @Modifying
    @Query("update GroupMessage m set m.readAt = :readAt where m.group.id = :groupId and m.author.id <> :userId and m.readAt is null")
    int markUnreadAsRead(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("readAt") java.time.LocalDateTime readAt);
}
