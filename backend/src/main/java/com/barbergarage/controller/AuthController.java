package com.barbergarage.controller;

import com.barbergarage.dto.ApiDtos.*;
import com.barbergarage.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserService users; public AuthController(UserService users){this.users=users;}
    @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest r){return users.login(r);}
    @GetMapping("/me") public UserView me(Authentication a){return users.view(users.byUsername(a.getName()));}
    @PostMapping("/change-password") public ResponseEntity<?> change(Authentication a,@Valid @RequestBody ChangePasswordRequest r){users.changePassword(users.byUsername(a.getName()),r);return ResponseEntity.ok().body(java.util.Map.of("message","Contraseña actualizada"));}
    @PostMapping("/profile-photo") public UserView photo(Authentication a,@Valid @RequestBody ProfilePhotoRequest r){return users.saveProfile(users.byUsername(a.getName()),r);}
}
