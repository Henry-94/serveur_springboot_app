package com.reseausocial.group.repository;
import com.reseausocial.group.entity.CampusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CampusEventRepository extends JpaRepository<CampusEvent, Long> { List<CampusEvent> findAllByOrderByStartsAtAsc(); }
