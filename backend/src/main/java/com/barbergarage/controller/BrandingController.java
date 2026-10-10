package com.barbergarage.controller;

import com.barbergarage.dto.ApiDtos.BrandingRequest;
import com.barbergarage.entity.BrandingSettings;
import com.barbergarage.repo.BrandingSettingsRepository;
import org.springframework.web.bind.annotation.*;

@RestController
public class BrandingController {
    private final BrandingSettingsRepository repo; public BrandingController(BrandingSettingsRepository repo){this.repo=repo;}
    @GetMapping("/api/public/branding") public BrandingSettings get(){return repo.findById(1L).orElseGet(()->repo.save(new BrandingSettings()));}
    @PutMapping("/api/admin/branding") public BrandingSettings update(@RequestBody BrandingRequest r){
        BrandingSettings b=repo.findById(1L).orElseGet(BrandingSettings::new);
        if(r.businessName()!=null&&!r.businessName().isBlank())b.setBusinessName(r.businessName());
        if(r.logoDataUrl()!=null)b.setLogoDataUrl(r.logoDataUrl()); if(r.loginBackgroundDataUrl()!=null)b.setLoginBackgroundDataUrl(r.loginBackgroundDataUrl()); if(r.appBackgroundDataUrl()!=null)b.setAppBackgroundDataUrl(r.appBackgroundDataUrl()); if(r.accentColor()!=null&&!r.accentColor().isBlank())b.setAccentColor(r.accentColor()); return repo.save(b);
    }
}
