// com.example.recrutement.notification.event.NotificationEventListener.java
package com.example.recrutement.notification.event;

import com.example.recrutement.notification.entity.NotificationType;
import com.example.recrutement.notification.entity.Priority;
import com.example.recrutement.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleCandidatureEvent(CandidatureEvent event) {
        log.info("📨 Événement reçu: {} pour candidature {}", event.getEventType(), event.getCandidatureId());

        switch (event.getEventType()) {
            case CREATED:
                // Notifier nouvelle candidature
                Long rhUserId = getRHUserIdForOffre(event.getOffreId());
                notificationService.notifyNewCandidature(
                    rhUserId,
                    event.getCandidatName(),
                    event.getOffreTitle(),
                    event.getCandidatureId(),
                    event.getMatchingScore()
                );
                break;

            case STATUS_CHANGED:
                // Notifier changement de statut si pertinent
                if ("ENTRETIEN".equals(event.getNouveauStatut())) {
                    // Notification pour statut "Entretien programmé"
                    log.info("📢 Statut changement détecté: {} -> {}", event.getAncienStatut(), event.getNouveauStatut());
                }
                break;
        }
    }

    @Async
    @EventListener
    public void handleInterviewEvent(InterviewEvent event) {
        log.info("📨 Événement reçu: {} pour entretien {}", event.getEventType(), event.getInterviewId());

        Long rhUserId = getCurrentRHUserId();

        switch (event.getEventType()) {
            case CREATED:
                notificationService.notifyInterviewScheduled(
                    rhUserId,
                    event.getCandidatName(),
                    event.getOffreTitle(),
                    event.getDateTime(),
                    event.getInterviewId()
                );
                break;

            case CONFIRMED:
                notificationService.notifyInterviewConfirmed(
                    rhUserId,
                    event.getCandidatName(),
                    event.getOffreTitle(),
                    event.getDateTime(),
                    event.getInterviewId()
                );
                break;

            case RESCHEDULED:
                notificationService.notifyInterviewReminder(
                    rhUserId,
                    event.getCandidatName(),
                    event.getOffreTitle(),
                    event.getDateTime(),
                    event.getInterviewId(),
                    24
                );
                break;
        }
    }

    private Long getRHUserIdForOffre(UUID offreId) {
        // TODO: Implémenter la récupération du RH associé à l'offre
        return 1L;
    }

    private Long getCurrentRHUserId() {
        // TODO: Implémenter la récupération du RH courant
        return 1L;
    }
}