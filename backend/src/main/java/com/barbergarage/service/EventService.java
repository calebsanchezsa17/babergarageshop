package com.barbergarage.service;

import com.barbergarage.dto.ApiDtos.EventRequest;
import com.barbergarage.entity.DetectionEvent;
import com.barbergarage.repo.DetectionEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EventService {
    private final DetectionEventRepository repo;
    private final CopyOnWriteArrayList<SseEmitter> emitters=new CopyOnWriteArrayList<>();
    public EventService(DetectionEventRepository repo){this.repo=repo;}
    public DetectionEvent save(EventRequest r){
        DetectionEvent e=new DetectionEvent();e.setBarberId(r.barberId());e.setStation(r.station());e.setEventType(r.eventType());e.setTool(r.tool());e.setAction(r.action());e.setConfidence(r.confidence());e.setDurationSeconds(r.durationSeconds());e.setDetail(r.detail());e.setSnapshotDataUrl(r.snapshotDataUrl());e.setOccurredAt(r.occurredAt()==null?Instant.now():r.occurredAt());
        e=repo.save(e); broadcast(e); return e;
    }
    public List<DetectionEvent> recent(int limit){return repo.findAllByOrderByOccurredAtDesc(PageRequest.of(0,Math.min(Math.max(limit,1),200)));}
    public List<DetectionEvent> recentFor(Long barberId,int limit){return repo.findByBarberIdOrderByOccurredAtDesc(barberId,PageRequest.of(0,Math.min(Math.max(limit,1),200)));}
    public SseEmitter subscribe(){
        SseEmitter em=new SseEmitter(0L);emitters.add(em);Runnable remove=()->emitters.remove(em);em.onCompletion(remove);em.onTimeout(remove);em.onError(ex->remove.run());
        try{em.send(SseEmitter.event().name("connected").data("ok"));}catch(IOException ignored){}
        return em;
    }
    private void broadcast(DetectionEvent e){for(SseEmitter em:emitters){try{em.send(SseEmitter.event().name("detection").data(e));}catch(Exception ex){emitters.remove(em);}}}
}
