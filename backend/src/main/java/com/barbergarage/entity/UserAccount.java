package com.barbergarage.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="users")
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=120) private String fullName;
    @Column(nullable=false, unique=true, length=160) private String email;
    @Column(nullable=false, unique=true, length=80) private String username;
    @Column(nullable=false, length=100) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private Role role;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private AccountStatus status = AccountStatus.ACTIVE;
    @Column(nullable=false) private int failedAttempts = 0;
    private Instant lockedUntil;
    @Column(nullable=false) private boolean onboardingComplete = false;
    @Lob @Column(columnDefinition="LONGTEXT") private String profilePhotoDataUrl;
    @Column(length=40) private String uniformColor;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;

    @PrePersist void prePersist(){ Instant now=Instant.now(); createdAt=now; updatedAt=now; }
    @PreUpdate void preUpdate(){ updatedAt=Instant.now(); }

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
    public Role getRole(){return role;} public void setRole(Role v){role=v;}
    public AccountStatus getStatus(){return status;} public void setStatus(AccountStatus v){status=v;}
    public int getFailedAttempts(){return failedAttempts;} public void setFailedAttempts(int v){failedAttempts=v;}
    public Instant getLockedUntil(){return lockedUntil;} public void setLockedUntil(Instant v){lockedUntil=v;}
    public boolean isOnboardingComplete(){return onboardingComplete;} public void setOnboardingComplete(boolean v){onboardingComplete=v;}
    public String getProfilePhotoDataUrl(){return profilePhotoDataUrl;} public void setProfilePhotoDataUrl(String v){profilePhotoDataUrl=v;}
    public String getUniformColor(){return uniformColor;} public void setUniformColor(String v){uniformColor=v;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
