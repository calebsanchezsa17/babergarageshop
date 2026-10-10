package com.barbergarage.config;

import com.barbergarage.entity.*;
import com.barbergarage.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users; private final BrandingSettingsRepository branding; private final PasswordEncoder encoder;
    @Value("${app.admin.username}") String username; @Value("${app.admin.email}") String email; @Value("${app.admin.password}") String password; @Value("${app.admin.full-name}") String fullName;
    public DataInitializer(UserRepository users,BrandingSettingsRepository branding,PasswordEncoder encoder){this.users=users;this.branding=branding;this.encoder=encoder;}
    @Override public void run(String... args){
        if(users.findByUsernameIgnoreCase(username).isEmpty()){
            UserAccount u=new UserAccount();u.setFullName(fullName);u.setEmail(email);u.setUsername(username);u.setPasswordHash(encoder.encode(password));u.setRole(Role.ADMIN);u.setStatus(AccountStatus.ACTIVE);u.setOnboardingComplete(true);users.save(u);
        }
        if(branding.findById(1L).isEmpty()) branding.save(new BrandingSettings());
    }
}
