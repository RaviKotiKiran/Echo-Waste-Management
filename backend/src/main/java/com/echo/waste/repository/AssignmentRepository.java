package com.echo.waste.repository;
import com.echo.waste.entity.CollectorAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface AssignmentRepository extends JpaRepository<CollectorAssignment,Long>{@EntityGraph(attributePaths={"request","request.user","collector"}) List<CollectorAssignment> findByCollectorIdOrderByAssignedAtDesc(Long collectorId);Optional<CollectorAssignment> findByRequestId(Long requestId);@EntityGraph(attributePaths="collector") List<CollectorAssignment> findAllByRequestIdIn(Collection<Long> requestIds);long countByCollectorId(Long collectorId);boolean existsByCollectorId(Long collectorId);}
