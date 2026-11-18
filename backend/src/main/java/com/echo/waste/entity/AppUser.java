package com.echo.waste.entity;
import com.echo.waste.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="users", indexes=@Index(name="idx_users_role", columnList="role"))
public class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="full_name", nullable=false, length=120) private String fullName;
    @Column(nullable=false, unique=true, length=180) private String email;
    @Column(nullable=false) private String password;
    @Column(length=30) private String phone;
    @Column(length=500) private String address;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private UserRole role;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    protected AppUser() { }
    public AppUser(String fullName,String email,String password,String phone,String address,UserRole role){this.fullName=fullName;this.email=email;this.password=password;this.phone=phone;this.address=address;this.role=role;}
    public Long getId(){return id;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v.toLowerCase();} public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public UserRole getRole(){return role;} public void setRole(UserRole v){role=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
