package com.echo.waste.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="feedback",uniqueConstraints=@UniqueConstraint(name="uk_feedback_request",columnNames="request_id"),indexes=@Index(name="idx_feedback_user",columnList="user_id"))
public class Feedback {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private AppUser user;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="request_id",nullable=false) private WasteRequest request;
    @Column(nullable=false) private int rating;
    @Column(length=1000) private String comments;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    protected Feedback() { }
    public Feedback(AppUser user,WasteRequest request,int rating,String comments){this.user=user;this.request=request;this.rating=rating;this.comments=comments;}
    public Long getId(){return id;} public AppUser getUser(){return user;} public WasteRequest getRequest(){return request;} public int getRating(){return rating;} public String getComments(){return comments;} public LocalDateTime getCreatedAt(){return createdAt;}
}
