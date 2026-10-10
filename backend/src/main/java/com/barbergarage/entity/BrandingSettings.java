package com.barbergarage.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="branding_settings")
public class BrandingSettings {
    @Id private Long id = 1L;
    @Column(nullable=false, length=100) private String businessName = "BARBER GARAGE";
    @Lob @Column(columnDefinition="LONGTEXT") private String logoDataUrl;
    @Lob @Column(columnDefinition="LONGTEXT") private String loginBackgroundDataUrl;
    @Lob @Column(columnDefinition="LONGTEXT") private String appBackgroundDataUrl;
    @Column(length=20) private String accentColor = "#c99545";
    @Column(nullable=false) private Instant updatedAt = Instant.now();
    @PreUpdate @PrePersist void touch(){updatedAt=Instant.now(); if(id==null) id=1L;}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getBusinessName(){return businessName;} public void setBusinessName(String v){businessName=v;}
    public String getLogoDataUrl(){return logoDataUrl;} public void setLogoDataUrl(String v){logoDataUrl=v;}
    public String getLoginBackgroundDataUrl(){return loginBackgroundDataUrl;} public void setLoginBackgroundDataUrl(String v){loginBackgroundDataUrl=v;}
    public String getAppBackgroundDataUrl(){return appBackgroundDataUrl;} public void setAppBackgroundDataUrl(String v){appBackgroundDataUrl=v;}
    public String getAccentColor(){return accentColor;} public void setAccentColor(String v){accentColor=v;}
    public Instant getUpdatedAt(){return updatedAt;}
}
