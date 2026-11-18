package com.echo.waste.repository;

import com.echo.waste.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findTop25ByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<UserNotification> findByRecipientIdAndReadAtIsNull(Long recipientId);
    Optional<UserNotification> findByIdAndRecipientId(Long id, Long recipientId);
    long countByRecipientIdAndReadAtIsNull(Long recipientId);
}
