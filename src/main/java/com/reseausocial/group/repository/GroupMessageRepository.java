package com.reseausocial.group.repository;
import com.reseausocial.group.entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupIdOrderBySentAtAsc(Long groupId);
    List<GroupMessage> findTop100ByGroupIdOrderBySentAtDesc(Long groupId);
    java.util.Optional<GroupMessage> findTop1ByGroupIdOrderBySentAtDesc(Long groupId);
}
