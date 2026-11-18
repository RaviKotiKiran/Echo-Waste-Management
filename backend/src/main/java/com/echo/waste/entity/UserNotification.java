package com.echo.waste.entity;

import com.echo.waste.enums.NotificationKind;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications", indexes = {
        @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_at"),
        @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, read_at")
})
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private AppUser recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationKind kind;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "link_path", nullable = false, length = 255)
    private String linkPath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "read_at")
    private LocalDateTime readAt;

    protected UserNotification() { }

    public UserNotification(AppUser recipient, NotificationKind kind, String title, String message, String linkPath) {
        this.recipient = recipient;
        this.kind = kind;
        this.title = title;
        this.message = message;
        this.linkPath = linkPath;
    }

    public Long getId() { return id; }
    public AppUser getRecipient() { return recipient; }
    public NotificationKind getKind() { return kind; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getLinkPath() { return linkPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
