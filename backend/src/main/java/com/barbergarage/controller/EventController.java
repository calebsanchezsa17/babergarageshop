package com.barbergarage.controller;

import com.barbergarage.entity.DetectionEvent;
import com.barbergarage.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

@RestController @RequestMapping("/api/events")
public class EventController {
    private final EventService events; private final UserService users;
    public EventController(EventService events,UserService users){this.events=events;this.users=users;}
    @GetMapping public List<DetectionEvent> recent(@RequestParam(defaultValue="100") int limit,Authentication a){var u=users.byUsername(a.getName());return u.getRole().name().equals("ADMIN")?events.recent(limit):events.recentFor(u.getId(),limit);}
    @GetMapping(value="/stream",produces="text/event-stream") public SseEmitter stream(){return events.subscribe();}
}
