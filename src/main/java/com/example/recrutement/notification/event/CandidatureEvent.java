// com.example.recrutement.notification.event.CandidatureEvent.java
package com.example.recrutement.notification.event;

import lombok.Getter;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

@Getter
public class CandidatureEvent extends ApplicationEvent {
    private final Long candidatureId;
    private final Long candidatId;
    private final UUID offreId;
    private final String candidatName;
    private final String offreTitle;
    private final int matchingScore;
    private final String ancienStatut;
    private final String nouveauStatut;
    private final EventType eventType;

    public enum EventType {
        CREATED, STATUS_CHANGED
    }

    public CandidatureEvent(Object source, Long candidatureId, Long candidatId, UUID offreId,
                            String candidatName, String offreTitle, int matchingScore, EventType eventType) {
        super(source);
        this.candidatureId = candidatureId;
        this.candidatId = candidatId;
        this.offreId = offreId;
        this.candidatName = candidatName;
        this.offreTitle = offreTitle;
        this.matchingScore = matchingScore;
        this.ancienStatut = null;
        this.nouveauStatut = null;
        this.eventType = eventType;
    }

    public CandidatureEvent(Object source, Long candidatureId, Long candidatId, UUID offreId,
                            String candidatName, String offreTitle, String ancienStatut, 
                            String nouveauStatut, EventType eventType) {
        super(source);
        this.candidatureId = candidatureId;
        this.candidatId = candidatId;
        this.offreId = offreId;
        this.candidatName = candidatName;
        this.offreTitle = offreTitle;
        this.matchingScore = 0;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.eventType = eventType;
    }
}