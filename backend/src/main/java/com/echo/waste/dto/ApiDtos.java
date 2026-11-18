package com.echo.waste.dto;
import com.echo.waste.enums.RequestStatus;
import com.echo.waste.enums.IssueStatus;
import com.echo.waste.enums.NotificationKind;
import com.echo.waste.enums.UserRole;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
public final class ApiDtos {
    private ApiDtos(){}
    public record Register(@NotBlank @Size(max=120) String fullName,@NotBlank @Email @Size(max=180) String email,@NotBlank @Size(min=8,max=72) String password,@Size(max=30) String phone,@Size(max=500) String address){}
    public record Login(@NotBlank @Email String email,@NotBlank String password){}
    public record UserView(Long id,String fullName,String email,String phone,String address,UserRole role,LocalDateTime createdAt){}
    public record AuthResponse(String token,UserView user){}
    public record CreateRequest(@NotBlank @Pattern(regexp="Household Waste|Plastic Waste|Paper Waste|E-Waste|Garden Waste|Metal Waste|Organic Waste|Construction Waste") String wasteType,@NotNull @DecimalMin("0.01") @Digits(integer=8,fraction=2) BigDecimal quantity,@NotBlank @Size(max=500) String pickupAddress,@NotNull @FutureOrPresent LocalDate pickupDate,@NotNull LocalTime pickupTime,@Size(max=1000) String instructions){}
    public record RequestView(Long id,Long userId,String residentName,String email,String phone,String wasteType,BigDecimal quantity,String pickupAddress,LocalDate pickupDate,LocalTime pickupTime,String instructions,RequestStatus status,String adminNote,Long assignmentId,Long collectorId,String collectorName,LocalDateTime createdAt){}
    public record Assign(@NotNull Long collectorId){}
    public record Decision(@Size(max=500) String note){}
    public record StatusChange(@NotNull RequestStatus status){}
    public record FeedbackCreate(@NotNull Long requestId,@Min(1) @Max(5) int rating,@Size(max=1000) String comments){}
    public record CollectorCreate(@NotBlank @Size(max=120) String fullName,@NotBlank @Email @Size(max=180) String email,@NotBlank @Size(min=8,max=72) String password,@Size(max=30) String phone,@Size(max=500) String address){}
    public record UserUpdate(@NotBlank @Size(max=120) String fullName,@Size(max=30) String phone,@Size(max=500) String address){}
    public record FeedbackView(Long id,Long userId,String residentName,Long requestId,int rating,String comments,LocalDateTime createdAt){}
    public record Dashboard(long totalUsers,long totalCollectors,long totalRequests,long pendingRequests,long approvedRequests,long assignedRequests,long inProgressRequests,long completedRequests,long cancelledRequests){}
    public record Message(String message){}
    public record IssueCreate(@NotBlank @Pattern(regexp="MISSED_PICKUP|DAMAGED_BIN|ILLEGAL_DUMPING|OTHER") String category,@NotBlank @Size(max=1000) String description,Long requestId){}
    public record IssueStatusChange(@NotNull IssueStatus status){}
    public record IssueView(Long id,Long userId,String residentName,String email,Long requestId,String category,String description,IssueStatus status,LocalDateTime createdAt,LocalDateTime updatedAt){}
    public record NotificationView(Long id,NotificationKind kind,String title,String message,String linkPath,boolean read,LocalDateTime createdAt){}
    public record NotificationSummary(List<NotificationView> notifications,long unreadCount){}
    public record CalendarView(String month,List<RequestView> pickups){}
}
