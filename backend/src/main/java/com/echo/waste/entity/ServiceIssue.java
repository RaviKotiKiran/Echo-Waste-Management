package com.echo.waste.entity;

import com.echo.waste.enums.IssueStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_issues", indexes = {
        @Index(name = "idx_issue_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_issue_status", columnList = "status")
})
public class ServiceIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private WasteRequest request;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status = IssueStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected ServiceIssue() { }

    public ServiceIssue(AppUser user, WasteRequest request, String category, String description) {
        this.user = user;
        this.request = request;
        this.category = category;
        this.description = description;
    }

    @PreUpdate
    void touch() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public WasteRequest getRequest() { return request; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
