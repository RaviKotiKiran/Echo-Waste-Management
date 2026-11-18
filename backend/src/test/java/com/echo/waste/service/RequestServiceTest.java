package com.echo.waste.service;

import com.echo.waste.dto.ApiDtos.Assign;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestServiceTest {

    private final Map<Long, WasteRequest> requestDb = new HashMap<>();
    private final Map<Long, AppUser> userDb = new HashMap<>();
    private final Map<Long, CollectorAssignment> assignmentDb = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(100);

    private WasteRequestRepository requestRepository;
    private UserRepository userRepository;
    private AssignmentRepository assignmentRepository;
    private NotificationService notificationService;
    private RequestService requestService;

    private AppUser resident;
    private AppUser otherResident;
    private AppUser collector;
    private WasteRequest sampleRequest;

    @BeforeEach
    void setUp() {
        requestDb.clear();
        userDb.clear();
        assignmentDb.clear();

        resident = new AppUser("Resident A", "resident.a@example.com", "pass", null, null, UserRole.CITIZEN);
        ReflectionTestUtils.setField(resident, "id", 1L);
        userDb.put(1L, resident);

        otherResident = new AppUser("Resident B", "resident.b@example.com", "pass", null, null, UserRole.CITIZEN);
        ReflectionTestUtils.setField(otherResident, "id", 2L);
        userDb.put(2L, otherResident);

        collector = new AppUser("Collector Bob", "collector@example.com", "pass", null, null, UserRole.COLLECTOR);
        ReflectionTestUtils.setField(collector, "id", 3L);
        userDb.put(3L, collector);

        sampleRequest = new WasteRequest(resident, "Household Waste", new BigDecimal("5.00"),
                "123 Pine St", LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Leave by fence");
        ReflectionTestUtils.setField(sampleRequest, "id", 100L);
        requestDb.put(100L, sampleRequest);

        requestRepository = (WasteRequestRepository) Proxy.newProxyInstance(
                WasteRequestRepository.class.getClassLoader(),
                new Class<?>[]{WasteRequestRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        WasteRequest req = (WasteRequest) args[0];
                        if (req.getId() == null) {
                            ReflectionTestUtils.setField(req, "id", idGen.incrementAndGet());
                        }
                        requestDb.put(req.getId(), req);
                        return req;
                    }
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        return Optional.ofNullable(requestDb.get(id));
                    }
                    return null;
                });

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        return Optional.ofNullable(userDb.get(id));
                    }
                    return null;
                });

        assignmentRepository = (AssignmentRepository) Proxy.newProxyInstance(
                AssignmentRepository.class.getClassLoader(),
                new Class<?>[]{AssignmentRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findByRequestId".equals(name)) {
                        Long reqId = (Long) args[0];
                        return assignmentDb.values().stream()
                                .filter(a -> a.getRequest().getId().equals(reqId))
                                .findFirst();
                    }
                    if ("findById".equals(name)) {
                        Long id = (Long) args[0];
                        return Optional.ofNullable(assignmentDb.get(id));
                    }
                    if ("save".equals(name)) {
                        CollectorAssignment a = (CollectorAssignment) args[0];
                        if (a.getId() == null) {
                            ReflectionTestUtils.setField(a, "id", idGen.incrementAndGet());
                        }
                        assignmentDb.put(a.getId(), a);
                        return a;
                    }
                    return null;
                });

        // Safe in-memory stub for NotificationService
        notificationService = new NotificationService(null, null) {
            @Override
            public void send(AppUser recipient, NotificationKind kind, String title, String message, String linkPath) {
                // In-memory test stub
            }

            @Override
            public void notifyAdministrators(NotificationKind kind, String title, String message, String linkPath) {
                // In-memory test stub
            }
        };

        requestService = new RequestService(requestRepository, userRepository, assignmentRepository, notificationService);
    }

    @Test
    @DisplayName("Should create a new waste request with PENDING status")
    void testCreateRequest() {
        CreateRequest input = new CreateRequest("Household Waste", new BigDecimal("5.00"),
                "123 Pine St", LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Leave by fence");

        RequestView view = requestService.create(input, resident);

        assertThat(view.id()).isNotNull();
        assertThat(view.wasteType()).isEqualTo("Household Waste");
        assertThat(view.status()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @DisplayName("Resident should be able to cancel own pending request")
    void testCancelOwnPendingRequest() {
        RequestView cancelled = requestService.cancel(100L, resident);

        assertThat(cancelled.status()).isEqualTo(RequestStatus.CANCELLED);
        assertThat(sampleRequest.getStatus()).isEqualTo(RequestStatus.CANCELLED);
    }

    @Test
    @DisplayName("Resident cannot cancel another resident's request")
    void testCannotCancelOtherResidentRequest() {
        assertThatThrownBy(() -> requestService.cancel(100L, otherResident))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("Cannot cancel a non-pending request")
    void testCannotCancelApprovedRequest() {
        sampleRequest.setStatus(RequestStatus.APPROVED);

        assertThatThrownBy(() -> requestService.cancel(100L, resident))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("Admin can approve a pending request with note")
    void testApproveRequest() {
        RequestView result = requestService.approve(100L, new Decision("Approved for pickup"));

        assertThat(result.status()).isEqualTo(RequestStatus.APPROVED);
        assertThat(sampleRequest.getAdminNote()).isEqualTo("Approved for pickup");
    }

    @Test
    @DisplayName("Cannot assign collector to request that has not been approved")
    void testCannotAssignPendingRequest() {
        assertThatThrownBy(() -> requestService.assign(100L, new Assign(3L)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(apiEx.getMessage()).contains("Approve the request before assigning");
                });
    }

    @Test
    @DisplayName("Collector can update status to IN_PROGRESS and then COLLECTED")
    void testCollectorStatusTransitions() {
        sampleRequest.setStatus(RequestStatus.APPROVED);
        requestService.assign(100L, new Assign(3L));

        CollectorAssignment assignment = assignmentDb.values().stream()
                .filter(a -> a.getRequest().getId().equals(100L))
                .findFirst()
                .orElseThrow();

        // Move to IN_PROGRESS
        RequestView progressView = requestService.updateCollectorStatus(assignment.getId(), RequestStatus.IN_PROGRESS, collector);
        assertThat(progressView.status()).isEqualTo(RequestStatus.IN_PROGRESS);

        // Move to COLLECTED
        RequestView collectedView = requestService.updateCollectorStatus(assignment.getId(), RequestStatus.COLLECTED, collector);
        assertThat(collectedView.status()).isEqualTo(RequestStatus.COLLECTED);
    }
}
