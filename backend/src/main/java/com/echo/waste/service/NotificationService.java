package com.echo.waste.service;

import com.echo.waste.dto.ApiDtos.NotificationSummary;
import com.echo.waste.dto.ApiDtos.NotificationView;
import com.echo.waste.entity.AppUser;
import com.echo.waste.entity.UserNotification;
import com.echo.waste.enums.NotificationKind;
import com.echo.waste.exception.ApiException;
import com.echo.waste.repository.NotificationRepository;
import com.echo.waste.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationService(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional
    public void send(AppUser recipient, NotificationKind kind, String title, String message, String linkPath) {
        notifications.save(new UserNotification(recipient, kind, title, message, linkPath));
    }

    @Transactional(readOnly = true)
    public NotificationSummary listFor(AppUser recipient) {
        List<NotificationView> items = notifications.findTop25ByRecipientIdOrderByCreatedAtDesc(recipient.getId())
                .stream().map(this::view).toList();
        return new NotificationSummary(items, notifications.countByRecipientIdAndReadAtIsNull(recipient.getId()));
    }

    @Transactional
    public NotificationSummary markRead(long notificationId, AppUser recipient) {
        UserNotification notification = notifications.findByIdAndRecipientId(notificationId, recipient.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found."));
        if (notification.getReadAt() == null) notification.setReadAt(LocalDateTime.now());
        return listFor(recipient);
    }

    @Transactional
    public NotificationSummary markAllRead(AppUser recipient) {
        LocalDateTime now = LocalDateTime.now();
        notifications.findByRecipientIdAndReadAtIsNull(recipient.getId())
                .forEach(notification -> notification.setReadAt(now));
        return listFor(recipient);
    }

    @Transactional
    public void notifyAdministrators(NotificationKind kind, String title, String message, String linkPath) {
        users.findByRole(com.echo.waste.enums.UserRole.ADMIN)
                .forEach(admin -> send(admin, kind, title, message, linkPath));
    }

    private NotificationView view(UserNotification item) {
        return new NotificationView(item.getId(), item.getKind(), item.getTitle(), item.getMessage(),
                item.getLinkPath(), item.getReadAt() != null, item.getCreatedAt());
    }
}
