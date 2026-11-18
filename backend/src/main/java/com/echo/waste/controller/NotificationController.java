package com.echo.waste.controller;

import com.echo.waste.dto.ApiDtos.NotificationSummary;
import com.echo.waste.entity.AppUser;
import com.echo.waste.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) { this.notifications = notifications; }

    @GetMapping
    public NotificationSummary list(@AuthenticationPrincipal AppUser recipient) {
        return notifications.listFor(recipient);
    }

    @PatchMapping("/{id}/read")
    public NotificationSummary markRead(@PathVariable long id, @AuthenticationPrincipal AppUser recipient) {
        return notifications.markRead(id, recipient);
    }

    @PostMapping("/read-all")
    public NotificationSummary markAllRead(@AuthenticationPrincipal AppUser recipient) {
        return notifications.markAllRead(recipient);
    }
}
