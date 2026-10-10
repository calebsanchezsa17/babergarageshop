package com.barbergarage.controller;

import com.barbergarage.dto.ApiDtos.EventRequest;
import com.barbergarage.entity.DetectionEvent;
import com.barbergarage.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/ai")
public class AiController {
    private final EventService events; private final String token;
    public AiController(EventService events,@Value("${app.ai.service-token}") String token){this.events=events;this.token=token;}
    @PostMapping("/events") public DetectionEvent event(@RequestHeader(value="X-AI-Token",required=false) String provided,@Valid @RequestBody EventRequest r){if(!token.equals(provided))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"AI token inválido");return events.save(r);}
    @GetMapping("/health") public java.util.Map<String,Object> health(){return java.util.Map.of("ok",true);}
}
