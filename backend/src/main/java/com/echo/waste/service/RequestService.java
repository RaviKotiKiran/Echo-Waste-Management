package com.echo.waste.service;

import com.echo.waste.dto.ApiDtos.Assign;
import com.echo.waste.dto.ApiDtos.CalendarView;
import com.echo.waste.dto.ApiDtos.CreateRequest;
import com.echo.waste.dto.ApiDtos.Decision;
import com.echo.waste.dto.ApiDtos.RequestView;
import com.echo.waste.entity.AppUser;
import com.echo.waste.entity.CollectorAssignment;
import com.echo.waste.entity.WasteRequest;
import com.echo.waste.enums.NotificationKind;
import com.echo.waste.enums.RequestStatus;
import com.echo.waste.enums.UserRole;
import com.echo.waste.exception.ApiException;
import com.echo.waste.repository.AssignmentRepository;
import com.echo.waste.repository.UserRepository;
import com.echo.waste.repository.WasteRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.YearMonth;
import java.util.stream.Collectors;

@Service
public class RequestService {
    private final WasteRequestRepository requests;
    private final UserRepository users;
    private final AssignmentRepository assignments;
    private final NotificationService notifications;

    public RequestService(WasteRequestRepository requests, UserRepository users,
                          AssignmentRepository assignments, NotificationService notifications) {
        this.requests = requests;
        this.users = users;
        this.assignments = assignments;
        this.notifications = notifications;
    }

    @Transactional
    public RequestView create(CreateRequest input, AppUser resident) {
        WasteRequest request = requests.save(new WasteRequest(resident, input.wasteType(), input.quantity(),
                input.pickupAddress().trim(), input.pickupDate(), input.pickupTime(), clean(input.instructions())));
        notifications.notifyAdministrators(NotificationKind.PICKUP, "New pickup request",
                reference(request) + " is waiting for review.", "/app/manage");
        return view(request);
    }

    @Transactional(readOnly = true)
    public List<RequestView> mine(AppUser resident) {
        return views(requests.findByUserIdOrderByCreatedAtDesc(resident.getId()));
    }

    @Transactional(readOnly = true)
    public CalendarView calendar(AppUser resident, YearMonth month) {
        var pickups = requests.findByUserIdAndPickupDateGreaterThanEqualAndPickupDateLessThanOrderByPickupDateAscPickupTimeAsc(
                resident.getId(), month.atDay(1), month.plusMonths(1).atDay(1));
        return new CalendarView(month.toString(), views(pickups));
    }

    @Transactional(readOnly = true)
    public List<RequestView> all() {
        return views(requests.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public RequestView get(long id, AppUser actor) {
        WasteRequest request = find(id);
        if (actor.getRole() == UserRole.CITIZEN && !request.getUser().getId().equals(actor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only view your own requests.");
        }
        if (actor.getRole() == UserRole.COLLECTOR) {
            CollectorAssignment assignment = assignments.findByRequestId(id)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "This request is not assigned to you."));
            if (!assignment.getCollector().getId().equals(actor.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "This request is not assigned to you.");
            }
        }
        return view(request);
    }

    @Transactional
    public RequestView cancel(long id, AppUser actor) {
        WasteRequest request = find(id);
        if (!request.getUser().getId().equals(actor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only cancel your own request.");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Only pending requests can be cancelled.");
        }
        request.setStatus(RequestStatus.CANCELLED);
        notifications.notifyAdministrators(NotificationKind.PICKUP, "Pickup request cancelled",
                reference(request) + " was cancelled by the resident.", "/app/manage");
        return view(request);
    }

    @Transactional
    public RequestView approve(long id, Decision decision) {
        WasteRequest request = find(id);
        requirePending(request, "Only pending requests can be approved.");
        request.setStatus(RequestStatus.APPROVED);
        request.setAdminNote(clean(decision.note()));
        notifications.send(request.getUser(), NotificationKind.PICKUP, "Pickup approved",
                reference(request) + " has been approved and is waiting for a collector.", "/app/requests");
        return view(request);
    }

    @Transactional
    public RequestView reject(long id, Decision decision) {
        WasteRequest request = find(id);
        requirePending(request, "Only pending requests can be rejected.");
        request.setStatus(RequestStatus.REJECTED);
        request.setAdminNote(clean(decision.note()));
        String reason = clean(decision.note());
        notifications.send(request.getUser(), NotificationKind.PICKUP, "Pickup request declined",
                reason == null ? reference(request) + " could not be approved. Contact the service team if you need help."
                        : reference(request) + " could not be approved: " + reason,
                "/app/requests");
        return view(request);
    }

    @Transactional
    public RequestView assign(long id, Assign input) {
        WasteRequest request = find(id);
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new ApiException(HttpStatus.CONFLICT, "Approve the request before assigning a collector.");
        }
        AppUser collector = users.findById(input.collectorId())
                .filter(user -> user.getRole() == UserRole.COLLECTOR)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Collector not found."));
        CollectorAssignment assignment = assignments.findByRequestId(id)
                .orElse(new CollectorAssignment(request, collector));
        assignment.setCollector(collector);
        assignment.setAssignmentStatus(RequestStatus.ASSIGNED.name());
        assignments.save(assignment);
        request.setStatus(RequestStatus.ASSIGNED);
        notifications.send(request.getUser(), NotificationKind.PICKUP, "Collector assigned",
                reference(request) + " has been assigned to a collection team.", "/app/requests");
        notifications.send(collector, NotificationKind.PICKUP, "Pickup assigned to you",
                reference(request) + " is ready in your assignments.", "/app/assignments");
        return view(request);
    }

    @Transactional(readOnly = true)
    public List<RequestView> collectorTasks(AppUser collector) {
        return views(assignments.findByCollectorIdOrderByAssignedAtDesc(collector.getId())
                .stream().map(CollectorAssignment::getRequest).toList());
    }

    @Transactional
    public RequestView updateCollectorStatus(long assignmentId, RequestStatus status, AppUser collector) {
        if (status != RequestStatus.IN_PROGRESS && status != RequestStatus.COLLECTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Collector status must be IN_PROGRESS or COLLECTED.");
        }
        CollectorAssignment assignment = assignments.findById(assignmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found."));
        if (!assignment.getCollector().getId().equals(collector.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This assignment belongs to another collector.");
        }
        WasteRequest request = assignment.getRequest();
        if (request.getStatus() != RequestStatus.ASSIGNED && request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new ApiException(HttpStatus.CONFLICT, "This assignment is no longer active.");
        }
        if (request.getStatus() == status) {
            throw new ApiException(HttpStatus.CONFLICT, "This pickup already has that status.");
        }
        request.setStatus(status);
        assignment.setAssignmentStatus(status.name());
        String title = status == RequestStatus.COLLECTED ? "Pickup completed" : "Collection started";
        String message = status == RequestStatus.COLLECTED
                ? reference(request) + " has been marked as collected."
                : "A collector has started " + reference(request) + ".";
        notifications.send(request.getUser(), NotificationKind.PICKUP, title, message, "/app/requests");
        return view(request);
    }

    private void requirePending(WasteRequest request, String message) {
        if (request.getStatus() != RequestStatus.PENDING) throw new ApiException(HttpStatus.CONFLICT, message);
    }

    private WasteRequest find(long id) {
        return requests.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found."));
    }

    private List<RequestView> views(List<WasteRequest> requestsToView) {
        if (requestsToView.isEmpty()) return List.of();
        var ids = requestsToView.stream().map(WasteRequest::getId).toList();
        Map<Long, CollectorAssignment> byRequest = assignments.findAllByRequestIdIn(ids).stream()
                .collect(Collectors.toMap(assignment -> assignment.getRequest().getId(), assignment -> assignment));
        return requestsToView.stream().map(request -> view(request, byRequest.get(request.getId()))).toList();
    }

    private RequestView view(WasteRequest request) {
        return view(request, assignments.findByRequestId(request.getId()).orElse(null));
    }

    private RequestView view(WasteRequest request, CollectorAssignment assignment) {
        AppUser owner = request.getUser();
        return new RequestView(request.getId(), owner.getId(), owner.getFullName(), owner.getEmail(), owner.getPhone(),
                request.getWasteType(), request.getQuantity(), request.getPickupAddress(), request.getPickupDate(),
                request.getPickupTime(), request.getInstructions(), request.getStatus(), request.getAdminNote(),
                assignment == null ? null : assignment.getId(),
                assignment == null ? null : assignment.getCollector().getId(),
                assignment == null ? null : assignment.getCollector().getFullName(), request.getCreatedAt());
    }

    private String reference(WasteRequest request) {
        return "EW-" + String.format(java.util.Locale.ROOT, "%04d", request.getId());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
