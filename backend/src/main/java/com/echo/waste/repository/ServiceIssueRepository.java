package com.echo.waste.repository;

import com.echo.waste.entity.ServiceIssue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceIssueRepository extends JpaRepository<ServiceIssue, Long> {
    @EntityGraph(attributePaths = {"user", "request"})
    List<ServiceIssue> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "request"})
    List<ServiceIssue> findAllByOrderByCreatedAtDesc();
}
