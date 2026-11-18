package com.echo.waste.repository;
import com.echo.waste.entity.WasteRequest;
import com.echo.waste.enums.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface WasteRequestRepository extends JpaRepository<WasteRequest,Long>{@EntityGraph(attributePaths="user") List<WasteRequest> findByUserIdOrderByCreatedAtDesc(Long userId);@EntityGraph(attributePaths="user") List<WasteRequest> findByUserIdAndPickupDateGreaterThanEqualAndPickupDateLessThanOrderByPickupDateAscPickupTimeAsc(Long userId,LocalDate from,LocalDate until);@EntityGraph(attributePaths="user") List<WasteRequest> findAllByOrderByCreatedAtDesc();long countByStatus(RequestStatus status);boolean existsByUserId(Long userId);}
