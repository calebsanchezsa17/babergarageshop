package com.barbergarage.repo;
import com.barbergarage.entity.DetectionEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DetectionEventRepository extends JpaRepository<DetectionEvent,Long>{
 List<DetectionEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);
 List<DetectionEvent> findByBarberIdOrderByOccurredAtDesc(Long barberId, Pageable pageable);
 long countByEventType(String eventType);
}
