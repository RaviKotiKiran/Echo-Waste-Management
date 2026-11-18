package com.echo.waste.repository;
import com.echo.waste.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FeedbackRepository extends JpaRepository<Feedback,Long>{boolean existsByRequestId(Long requestId);List<Feedback> findAllByOrderByCreatedAtDesc();boolean existsByUserId(Long userId);}
