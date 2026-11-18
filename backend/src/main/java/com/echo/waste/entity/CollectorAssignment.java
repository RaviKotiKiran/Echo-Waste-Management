package com.echo.waste.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="collector_assignments",uniqueConstraints=@UniqueConstraint(name="uk_assignment_request",columnNames="request_id"),indexes=@Index(name="idx_assignment_collector",columnList="collector_id"))
public class CollectorAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="request_id",nullable=false) private WasteRequest request;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="collector_id",nullable=false) private AppUser collector;
    @Column(name="assigned_at",nullable=false) private LocalDateTime assignedAt=LocalDateTime.now();
    @Column(name="assignment_status",nullable=false,length=20) private String assignmentStatus="ASSIGNED";
    protected CollectorAssignment() { }
    public CollectorAssignment(WasteRequest request,AppUser collector){this.request=request;this.collector=collector;}
    public Long getId(){return id;} public WasteRequest getRequest(){return request;} public AppUser getCollector(){return collector;} public void setCollector(AppUser v){collector=v;}
    public LocalDateTime getAssignedAt(){return assignedAt;} public String getAssignmentStatus(){return assignmentStatus;} public void setAssignmentStatus(String v){assignmentStatus=v;}
}
