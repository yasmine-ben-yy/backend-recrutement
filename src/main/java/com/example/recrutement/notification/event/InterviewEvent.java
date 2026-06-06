// com.example.recrutement.notification.event.InterviewEvent.java
package com.example.recrutement.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

@Getter
public class InterviewEvent extends ApplicationEvent {
    private final Long interviewId;
    private final Long candidatureId;
    private final String candidatName;
    private final String offreTitle;
    private final LocalDateTime dateTime;
    private final EventType eventType;

    public enum EventType {
        CREATED, CONFIRMED, CANCELLED, RESCHEDULED
    }

    public InterviewEvent(Object source, Long interviewId, Long candidatureId,
                          String candidatName, String offreTitle, LocalDateTime dateTime, EventType eventType) {
        super(source);
        this.interviewId = interviewId;
        this.candidatureId = candidatureId;
        this.candidatName = candidatName;
        this.offreTitle = offreTitle;
        this.dateTime = dateTime;
        this.eventType = eventType;
    }
}