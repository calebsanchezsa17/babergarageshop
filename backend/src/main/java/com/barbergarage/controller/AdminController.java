package com.barbergarage.controller;

import com.barbergarage.dto.ApiDtos.*;
import com.barbergarage.entity.*;
import com.barbergarage.repo.*;
import com.barbergarage.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/admin")
public class AdminController {
    private final UserService users; private final UserRepository userRepo; private final DetectionEventRepository eventRepo;
    public AdminController(UserService users,UserRepository userRepo,DetectionEventRepository eventRepo){this.users=users;this.userRepo=userRepo;this.eventRepo=eventRepo;}
    @GetMapping("/barbers") public List<UserView> barbers(){return users.barbers();}
    @PostMapping("/barbers") public UserView create(@Valid @RequestBody CreateBarberRequest r){return users.createBarber(r);}
    @PostMapping("/barbers/{id}/unlock") public UserView unlock(@PathVariable Long id){return users.unlock(id);}
    @PostMapping("/barbers/{id}/toggle") public UserView toggle(@PathVariable Long id){return users.toggle(id);}
    @GetMapping("/stats") public DashboardStats stats(){
        var all=userRepo.findAll(); long total=all.stream().filter(u->u.getRole()==Role.BARBER).count(); long active=all.stream().filter(u->u.getRole()==Role.BARBER&&u.getStatus()==AccountStatus.ACTIVE).count(); long locked=all.stream().filter(u->u.getRole()==Role.BARBER&&u.getStatus()==AccountStatus.LOCKED).count();
        return new DashboardStats(total,active,locked,eventRepo.count(),eventRepo.countByEventType("CORTE_CONTABILIZADO"));
    }
}
