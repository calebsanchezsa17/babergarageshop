
package com.barbergarage.config;

import com.barbergarage.entity.*;
import com.barbergarage.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final BrandingSettingsRepository branding;
    private final PasswordEncoder encoder;

    @Value("${app.admin.username}")
    String username;

    @Value("${app.admin.email}")
    String email;

    @Value("${app.admin.password}")
    String password;

    @Value("${app.admin.full-name}")
    String fullName;

    public DataInitializer(
            UserRepository users,
            BrandingSettingsRepository branding,
            PasswordEncoder encoder) {
        this.users = users;
        this.branding = branding;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {

        UserAccount admin = users.findByUsernameIgnoreCase(username)
                .or(() -> users.findByEmailIgnoreCase(email))
                .orElse(null);

        if (admin == null) {
            admin = new UserAccount();
            admin.setFullName(fullName);
            admin.setEmail(email);
            admin.setUsername(username);
            admin.setRole(Role.ADMIN);
            admin.setStatus(AccountStatus.ACTIVE);
            admin.setOnboardingComplete(true);
        }

        // Actualizar contraseña del administrador
        admin.setPasswordHash(encoder.encode(password));
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        admin.setStatus(AccountStatus.ACTIVE);

        users.save(admin);

        if (branding.findById(1L).isEmpty()) {
            branding.save(new BrandingSettings());
        }
    }
}
