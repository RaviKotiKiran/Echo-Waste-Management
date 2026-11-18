package com.echo.waste.service;

import com.echo.waste.dto.ApiDtos.IssueCreate;
import com.echo.waste.dto.ApiDtos.IssueStatusChange;
import com.echo.waste.dto.ApiDtos.IssueView;
import com.echo.waste.entity.AppUser;
import com.echo.waste.entity.ServiceIssue;
import com.echo.waste.entity.WasteRequest;
import com.echo.waste.enums.IssueStatus;
import com.echo.waste.enums.NotificationKind;
import com.echo.waste.exception.ApiException;
import com.echo.waste.repository.ServiceIssueRepository;
import com.echo.waste.repository.WasteRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceIssueService {
    private final ServiceIssueRepository issues;
    private final WasteRequestRepository requests;
    private final NotificationService notifications;

    public ServiceIssueService(ServiceIssueRepository issues, WasteRequestRepository requests, NotificationService notifications) {
        this.issues = issues;
        this.requests = requests;
        this.notifications = notifications;
    }

    @Transactional
    public IssueView create(IssueCreate input, AppUser resident) {
        WasteRequest request = null;
        if (input.requestId() != null) {
            request = requests.findById(input.requestId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pickup request not found."));
            if (!request.getUser().getId().equals(resident.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You can only report an issue for your own pickup.");
            }
        }
        var issue = issues.save(new ServiceIssue(resident, request, input.category(), input.description().trim()));
        notifications.notifyAdministrators(NotificationKind.ISSUE, "New service report",
                "Report #" + String.format(java.util.Locale.ROOT, "%04d", issue.getId()) + " needs review.",
                "/app/issue-queue");
        return view(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueView> mine(AppUser resident) {
        return issues.findByUserIdOrderByCreatedAtDesc(resident.getId()).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<IssueView> all() {
        return issues.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    @Transactional
    public IssueView updateStatus(long id, IssueStatusChange input) {
        ServiceIssue issue = issues.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Service issue not found."));
        if (issue.getStatus() == input.status()) {
            throw new ApiException(HttpStatus.CONFLICT, "This issue already has that status.");
        }
        if (issue.getStatus() == IssueStatus.RESOLVED) {
            throw new ApiException(HttpStatus.CONFLICT, "Resolved issues cannot be changed.");
        }
        issue.setStatus(input.status());
        String title = input.status() == IssueStatus.RESOLVED ? "Service report resolved" : "Service report in review";
        String message = input.status() == IssueStatus.RESOLVED
                ? "Your report #" + String.format(java.util.Locale.ROOT, "%04d", issue.getId()) + " has been resolved."
                : "The service team is reviewing report #" + String.format(java.util.Locale.ROOT, "%04d", issue.getId()) + ".";
        notifications.send(issue.getUser(), NotificationKind.ISSUE, title, message, "/app/issues");
        return view(issue);
    }

    private IssueView view(ServiceIssue issue) {
        AppUser resident = issue.getUser();
        return new IssueView(issue.getId(), resident.getId(), resident.getFullName(), resident.getEmail(),
                issue.getRequest() == null ? null : issue.getRequest().getId(), issue.getCategory(),
                issue.getDescription(), issue.getStatus(), issue.getCreatedAt(), issue.getUpdatedAt());
    }
}
