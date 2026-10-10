package com.barbergarage.dto;

import com.barbergarage.entity.AccountStatus;
import com.barbergarage.entity.Role;
import jakarta.validation.constraints.*;
import java.time.Instant;

public final class ApiDtos {
    private ApiDtos(){}

    public record LoginRequest(@NotBlank String usernameOrEmail,@NotBlank String password){}
    public record LoginResponse(String token, UserView user){}
    public record CreateBarberRequest(
        @NotBlank @Size(max=120) String fullName,
        @Email @NotBlank String email,
        @NotBlank @Size(min=3,max=80) String username,
        @NotBlank @Size(min=8,max=80) String password,
        String uniformColor
    ){}
    public record ChangePasswordRequest(@NotBlank String currentPassword,@NotBlank @Size(min=8,max=80) String newPassword){}
    public record ProfilePhotoRequest(@NotBlank String photoDataUrl, String uniformColor){}
    public record UserView(Long id,String fullName,String email,String username,Role role,AccountStatus status,int failedAttempts,boolean onboardingComplete,String profilePhotoDataUrl,String uniformColor,Instant createdAt){}
    public record BrandingRequest(String businessName,String logoDataUrl,String loginBackgroundDataUrl,String appBackgroundDataUrl,String accentColor){}
    public record EventRequest(Long barberId,String station,@NotBlank String eventType,String tool,String action,Double confidence,Long durationSeconds,String detail,String snapshotDataUrl,Instant occurredAt){}
    public record DashboardStats(long totalBarbers,long activeBarbers,long lockedBarbers,long totalEvents,long countedCuts){}
}
