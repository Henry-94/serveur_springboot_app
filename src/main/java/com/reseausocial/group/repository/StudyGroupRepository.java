package com.reseausocial.group.repository;
import com.reseausocial.group.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    List<StudyGroup> findAllByOrderByCreatedAtDesc();
}
