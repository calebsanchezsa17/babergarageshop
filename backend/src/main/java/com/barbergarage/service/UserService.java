package com.barbergarage.service;

import com.barbergarage.dto.ApiDtos.*;
import com.barbergarage.entity.*;
import com.barbergarage.repo.UserRepository;
import com.barbergarage.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class UserService {
    private final UserRepository repo; private final PasswordEncoder encoder; private final JwtService jwt;
    private final int maxAttempts; private final int lockMinutes;
    public UserService(UserRepository repo,PasswordEncoder encoder,JwtService jwt,
        @Value("${app.login.max-failed-attempts}") int maxAttempts,@Value("${app.login.lock-minutes}") int lockMinutes){
        this.repo=repo;this.encoder=encoder;this.jwt=jwt;this.maxAttempts=maxAttempts;this.lockMinutes=lockMinutes;
    }
    public UserView view(UserAccount u){return new UserView(u.getId(),u.getFullName(),u.getEmail(),u.getUsername(),u.getRole(),u.getStatus(),u.getFailedAttempts(),u.isOnboardingComplete(),u.getProfilePhotoDataUrl(),u.getUniformColor(),u.getCreatedAt());}
    private UserAccount findLogin(String key){return repo.findByUsernameIgnoreCase(key).or(()->repo.findByEmailIgnoreCase(key)).orElseThrow(()->new IllegalArgumentException("Credenciales inválidas"));}
    @Transactional public LoginResponse login(LoginRequest r){
        UserAccount u=findLogin(r.usernameOrEmail());
        if(u.getStatus()==AccountStatus.DISABLED) throw new IllegalStateException("Cuenta deshabilitada");
        if(u.getStatus()==AccountStatus.LOCKED && u.getLockedUntil()!=null && Instant.now().isBefore(u.getLockedUntil())) throw new IllegalStateException("Cuenta bloqueada temporalmente");
        if(u.getStatus()==AccountStatus.LOCKED){u.setStatus(AccountStatus.ACTIVE);u.setFailedAttempts(0);u.setLockedUntil(null);}
        if(!encoder.matches(r.password(),u.getPasswordHash())){
            int n=u.getFailedAttempts()+1;u.setFailedAttempts(n);
            if(n>=maxAttempts){u.setStatus(AccountStatus.LOCKED);u.setLockedUntil(Instant.now().plus(Duration.ofMinutes(lockMinutes)));}
            repo.save(u); throw new IllegalArgumentException("Credenciales inválidas");
        }
        u.setFailedAttempts(0);u.setLockedUntil(null);u.setStatus(AccountStatus.ACTIVE);repo.save(u);
        return new LoginResponse(jwt.generate(u),view(u));
    }
    public UserAccount byUsername(String username){return repo.findByUsernameIgnoreCase(username).orElseThrow();}
    @Transactional public UserView createBarber(CreateBarberRequest r){
        if(repo.existsByUsernameIgnoreCase(r.username())) throw new IllegalArgumentException("El usuario ya existe");
        if(repo.existsByEmailIgnoreCase(r.email())) throw new IllegalArgumentException("El correo ya existe");
        UserAccount u=new UserAccount();u.setFullName(r.fullName());u.setEmail(r.email().toLowerCase());u.setUsername(r.username().toLowerCase());u.setPasswordHash(encoder.encode(r.password()));u.setRole(Role.BARBER);u.setStatus(AccountStatus.ACTIVE);u.setOnboardingComplete(false);u.setUniformColor(r.uniformColor());
        return view(repo.save(u));
    }
    @Transactional public UserView saveProfile(UserAccount u, ProfilePhotoRequest r){u.setProfilePhotoDataUrl(r.photoDataUrl());u.setUniformColor(r.uniformColor());u.setOnboardingComplete(true);return view(repo.save(u));}
    @Transactional public void changePassword(UserAccount u,ChangePasswordRequest r){if(!encoder.matches(r.currentPassword(),u.getPasswordHash())) throw new IllegalArgumentException("Contraseña actual incorrecta");u.setPasswordHash(encoder.encode(r.newPassword()));repo.save(u);}
    @Transactional public UserView unlock(Long id){UserAccount u=repo.findById(id).orElseThrow();u.setStatus(AccountStatus.ACTIVE);u.setFailedAttempts(0);u.setLockedUntil(null);return view(repo.save(u));}
    @Transactional public UserView toggle(Long id){UserAccount u=repo.findById(id).orElseThrow();u.setStatus(u.getStatus()==AccountStatus.DISABLED?AccountStatus.ACTIVE:AccountStatus.DISABLED);return view(repo.save(u));}
    public List<UserView> barbers(){return repo.findAll().stream().filter(u->u.getRole()==Role.BARBER).sorted(Comparator.comparing(UserAccount::getCreatedAt).reversed()).map(this::view).toList();}
}
