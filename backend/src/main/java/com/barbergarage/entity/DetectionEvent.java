package com.barbergarage.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="detection_events")
public class DetectionEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long barberId;
    @Column(length=80) private String station;
    @Column(nullable=false, length=60) private String eventType;
    @Column(length=80) private String tool;
    @Column(length=120) private String action;
    private Double confidence;
    private Long durationSeconds;
    @Column(length=500) private String detail;
    @Lob @Column(columnDefinition="LONGTEXT") private String snapshotDataUrl;
    @Column(nullable=false) private Instant occurredAt;

    @PrePersist void prePersist(){ if(occurredAt==null) occurredAt=Instant.now(); }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getBarberId(){return barberId;} public void setBarberId(Long v){barberId=v;}
    public String getStation(){return station;} public void setStation(String v){station=v;}
    public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
    public String getTool(){return tool;} public void setTool(String v){tool=v;}
    public String getAction(){return action;} public void setAction(String v){action=v;}
    public Double getConfidence(){return confidence;} public void setConfidence(Double v){confidence=v;}
    public Long getDurationSeconds(){return durationSeconds;} public void setDurationSeconds(Long v){durationSeconds=v;}
    public String getDetail(){return detail;} public void setDetail(String v){detail=v;}
    public String getSnapshotDataUrl(){return snapshotDataUrl;} public void setSnapshotDataUrl(String v){snapshotDataUrl=v;}
    public Instant getOccurredAt(){return occurredAt;} public void setOccurredAt(Instant v){occurredAt=v;}
}
