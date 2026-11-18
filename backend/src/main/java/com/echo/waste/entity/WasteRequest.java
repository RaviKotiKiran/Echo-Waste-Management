package com.echo.waste.entity;
import com.echo.waste.enums.RequestStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Entity @Table(name="waste_requests", indexes={@Index(name="idx_request_user",columnList="user_id"),@Index(name="idx_request_status",columnList="status"),@Index(name="idx_request_date",columnList="pickup_date")})
public class WasteRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private AppUser user;
    @Column(name="waste_type",nullable=false,length=40) private String wasteType;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal quantity;
    @Column(name="pickup_address",nullable=false,length=500) private String pickupAddress;
    @Column(name="pickup_date",nullable=false) private LocalDate pickupDate;
    @Column(name="pickup_time",nullable=false) private LocalTime pickupTime;
    @Column(length=1000) private String instructions;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private RequestStatus status=RequestStatus.PENDING;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    @Column(length=500) private String adminNote;
    protected WasteRequest() { }
    public WasteRequest(AppUser user,String wasteType,BigDecimal quantity,String pickupAddress,LocalDate pickupDate,LocalTime pickupTime,String instructions){this.user=user;this.wasteType=wasteType;this.quantity=quantity;this.pickupAddress=pickupAddress;this.pickupDate=pickupDate;this.pickupTime=pickupTime;this.instructions=instructions;}
    public Long getId(){return id;} public AppUser getUser(){return user;} public String getWasteType(){return wasteType;} public BigDecimal getQuantity(){return quantity;}
    public String getPickupAddress(){return pickupAddress;} public LocalDate getPickupDate(){return pickupDate;} public LocalTime getPickupTime(){return pickupTime;}
    public String getInstructions(){return instructions;} public RequestStatus getStatus(){return status;} public void setStatus(RequestStatus v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public String getAdminNote(){return adminNote;} public void setAdminNote(String v){adminNote=v;}
}
