// com.example.recrutement.interview.service.InterviewService.java
package com.example.recrutement.interview.service;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.entity.StatutCandidature;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import com.example.recrutement.interview.dto.InterviewRequest;
import com.example.recrutement.interview.dto.InterviewResponse;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.interview.entity.InterviewCommentaire;
import com.example.recrutement.interview.exception.InterviewNotFoundException;
import com.example.recrutement.interview.exception.InterviewValidationException;
import com.example.recrutement.interview.repository.InterviewRepository;
import com.example.recrutement.notification.event.InterviewEvent;
import com.example.recrutement.service.IEmailService;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidatureRepository candidatureRepository;
    private final UserRepository userRepository;
    private final IEmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    // ==================== CREATE ====================

    public InterviewResponse createInterview(InterviewRequest request, String userEmail) {
        log.info("Création d'un entretien par: {}", userEmail);

        User rh = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InterviewValidationException("RH non trouvé"));

        Candidature candidature = candidatureRepository.findById(request.getCandidatureId())
                .orElseThrow(() -> new InterviewValidationException("Candidature non trouvée"));

        validateInterviewRequest(request);
        checkScheduleConflict(request.getDate(), request.getDuree(), null);

        Interview interview = Interview.builder()
                .candidature(candidature)
                .type(request.getType())
                .statut(Interview.InterviewStatus.PLANIFIE)
                .dateEntretien(request.getDate())
                .dureeMinutes(request.getDuree())
                .lieu(request.getLieu())
                .meetingLink(request.getMeetingLink())
                .notes(request.getNotes())
                .createdBy(rh)
                .build();

        Interview saved = interviewRepository.save(interview);
        log.info("✅ Entretien créé avec ID: {}", saved.getId());

        if (candidature.getStatut() != StatutCandidature.ENTRETIEN) {
            candidature.setStatut(StatutCandidature.ENTRETIEN);
            candidatureRepository.save(candidature);
            log.info("📝 Statut candidature {} mis à jour vers ENTRETIEN", candidature.getId());
        }

        // Publication de l'événement de création
        try {
            String candidatName = candidature.getCandidate().getPrenom() + " " + candidature.getCandidate().getNom();
            String offreTitle = candidature.getOffre().getTitre();
            
            eventPublisher.publishEvent(new InterviewEvent(
                this,
                saved.getId(),
                candidature.getId(),
                candidatName,
                offreTitle,
                saved.getDateEntretien(),
                InterviewEvent.EventType.CREATED
            ));
            log.info("📢 Événement de création d'entretien publié");
        } catch (Exception e) {
            log.error("❌ Erreur publication événement entretien: {}", e.getMessage());
        }

        envoyerEmailEntretien(candidature, saved, "Veuillez confirmer votre disponibilité pour cet entretien.");

        return InterviewResponse.fromEntity(saved);
    }

    // ==================== UPDATE ====================

    public InterviewResponse updateInterview(Long id, InterviewRequest request, String userEmail) {
        log.info("Mise à jour de l'entretien: {}", id);

        Interview interview = getInterviewEntityById(id);
        Candidature candidature = interview.getCandidature();

        if (!interview.getCreatedBy().getEmail().equals(userEmail)) {
            throw new InterviewValidationException("Non autorisé");
        }

        if (!interview.estModifiable()) {
            throw new InterviewValidationException("Cet entretien n'est plus modifiable");
        }

        String ancienneDate = interview.getDateEntretien().format(DATE_TIME_FORMATTER);
        boolean dateChanged = false;

        checkScheduleConflict(request.getDate(), request.getDuree(), id);

        if (request.getType() != null) interview.setType(request.getType());
        if (request.getDate() != null) {
            interview.setDateEntretien(request.getDate());
            dateChanged = true;
        }
        if (request.getDuree() != null) interview.setDureeMinutes(request.getDuree());
        if (request.getLieu() != null) interview.setLieu(request.getLieu());
        if (request.getMeetingLink() != null) interview.setMeetingLink(request.getMeetingLink());
        if (request.getNotes() != null) interview.setNotes(request.getNotes());

        Interview updated = interviewRepository.save(interview);
        log.info("✅ Entretien mis à jour: {}", id);

        if (dateChanged) {
            try {
                String candidatName = candidature.getCandidate().getPrenom() + " " + candidature.getCandidate().getNom();
                String offreTitle = candidature.getOffre().getTitre();
                
                eventPublisher.publishEvent(new InterviewEvent(
                    this,
                    interview.getId(),
                    candidature.getId(),
                    candidatName,
                    offreTitle,
                    interview.getDateEntretien(),
                    InterviewEvent.EventType.RESCHEDULED
                ));
                log.info("📢 Événement de report d'entretien publié");
            } catch (Exception e) {
                log.error("❌ Erreur publication événement report: {}", e.getMessage());
            }
        }

        envoyerEmailMiseAJour(candidature, updated, ancienneDate,
            "Veuillez prendre note du changement de date pour votre entretien.");

        return InterviewResponse.fromEntity(updated);
    }

    // ==================== READ ====================

    public InterviewResponse getInterviewById(Long id) {
        return InterviewResponse.fromEntity(getInterviewEntityById(id));
    }

    public List<InterviewResponse> getAllInterviews() {
        return interviewRepository.findAll().stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InterviewResponse> getByCandidature(Long candidatureId) {
        return interviewRepository.findByCandidatureId(candidatureId)
                .stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InterviewResponse> getInterviewsByCandidat(Long candidatId) {
        return interviewRepository.findByCandidatId(candidatId)
                .stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InterviewResponse> getInterviewsByOffre(UUID offreId) {
        return interviewRepository.findByOffreId(offreId)
                .stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InterviewResponse> getUpcomingInterviews() {
        return interviewRepository.findUpcomingInterviews(LocalDateTime.now())
                .stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== DELETE ====================

    public void deleteInterview(Long id, String userEmail) {
        log.info("Suppression entretien {} par {}", id, userEmail);

        Interview interview = getInterviewEntityById(id);

        if (!interview.getCreatedBy().getEmail().equals(userEmail)) {
            throw new InterviewValidationException("Non autorisé");
        }

        interviewRepository.delete(interview);
        log.info("✅ Entretien supprimé: {}", id);
    }

    // ==================== STATUS ====================

    public InterviewResponse updateStatus(Long id, Interview.InterviewStatus statut, String userEmail) {
        log.info("Mise à jour statut entretien {} -> {} par {}", id, statut, userEmail);

        Interview interview = getInterviewEntityById(id);
        Candidature candidature = interview.getCandidature();
        
        interview.setStatut(statut);
        Interview updated = interviewRepository.save(interview);
        
        try {
            String candidatName = candidature.getCandidate().getPrenom() + " " + candidature.getCandidate().getNom();
            String offreTitle = candidature.getOffre().getTitre();
            
            InterviewEvent.EventType eventType;
            if (statut == Interview.InterviewStatus.CONFIRME) {
                eventType = InterviewEvent.EventType.CONFIRMED;
            } else if (statut == Interview.InterviewStatus.ANNULE) {
                eventType = InterviewEvent.EventType.CANCELLED;
            } else {
                eventType = InterviewEvent.EventType.RESCHEDULED;
            }
            
            eventPublisher.publishEvent(new InterviewEvent(
                this,
                interview.getId(),
                candidature.getId(),
                candidatName,
                offreTitle,
                interview.getDateEntretien(),
                eventType
            ));
            log.info("📢 Événement de changement de statut d'entretien publié: {}", statut);
        } catch (Exception e) {
            log.error("❌ Erreur publication événement statut entretien: {}", e.getMessage());
        }
        
        if (statut == Interview.InterviewStatus.CONFIRME) {
            envoyerEmailConfirmation(candidature, updated, "Votre entretien a été confirmé.");
        } else if (statut == Interview.InterviewStatus.ANNULE) {
            envoyerEmailAnnulation(candidature, updated, "L'entretien a été annulé.");
        }

        return InterviewResponse.fromEntity(updated);
    }

    public InterviewResponse confirmInterview(Long id, String userEmail) {
        return updateStatus(id, Interview.InterviewStatus.CONFIRME, userEmail);
    }

    public InterviewResponse cancelInterview(Long id, String userEmail) {
        return updateStatus(id, Interview.InterviewStatus.ANNULE, userEmail);
    }

    public InterviewResponse postponeInterview(Long id, LocalDateTime newDate, String userEmail) {
        log.info("Report entretien {} à {} par {}", id, newDate, userEmail);

        Interview interview = getInterviewEntityById(id);
        Candidature candidature = interview.getCandidature();
        
        checkScheduleConflict(newDate, interview.getDureeMinutes(), id);

        String ancienneDate = interview.getDateEntretien().format(DATE_TIME_FORMATTER);
        
        interview.setDateEntretien(newDate);
        interview.setStatut(Interview.InterviewStatus.REPORTE);
        
        Interview updated = interviewRepository.save(interview);
        
        try {
            String candidatName = candidature.getCandidate().getPrenom() + " " + candidature.getCandidate().getNom();
            String offreTitle = candidature.getOffre().getTitre();
            
            eventPublisher.publishEvent(new InterviewEvent(
                this,
                interview.getId(),
                candidature.getId(),
                candidatName,
                offreTitle,
                newDate,
                InterviewEvent.EventType.RESCHEDULED
            ));
            log.info("📢 Événement de report d'entretien publié");
        } catch (Exception e) {
            log.error("❌ Erreur publication événement report: {}", e.getMessage());
        }
        
        envoyerEmailMiseAJour(candidature, updated, ancienneDate, "L'entretien a été reporté.");

        return InterviewResponse.fromEntity(updated);
    }

    // ==================== NOTES & COMMENTAIRES ====================

    public InterviewResponse addNote(Long id, String note, String userEmail) {
        log.info("Ajout note entretien {} par {}", id, userEmail);

        Interview interview = getInterviewEntityById(id);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String existing = interview.getNotes();

        if (existing != null && !existing.isEmpty()) {
            interview.setNotes(existing + "\n\n[" + timestamp + "] " + note);
        } else {
            interview.setNotes("[" + timestamp + "] " + note);
        }

        return InterviewResponse.fromEntity(interviewRepository.save(interview));
    }

    public InterviewResponse addCommentaire(Long id, String contenu, String userEmail) {
        log.info("Ajout commentaire entretien {} par {}", id, userEmail);

        Interview interview = getInterviewEntityById(id);
        User rh = userRepository.findByEmail(userEmail).orElse(null);

        InterviewCommentaire commentaire = InterviewCommentaire.builder()
                .contenu(contenu)
                .createdBy(rh)
                .interview(interview)
                .build();

        interview.ajouterCommentaire(commentaire);

        return InterviewResponse.fromEntity(interviewRepository.save(interview));
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private Interview getInterviewEntityById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Entretien non trouvé avec l'ID: " + id));
    }

    private void validateInterviewRequest(InterviewRequest request) {
        if (request.getDate() == null) {
            throw new InterviewValidationException("La date de l'entretien est requise");
        }
        
        if (request.getDate().isBefore(LocalDateTime.now())) {
            throw new InterviewValidationException("La date de l'entretien ne peut pas être dans le passé");
        }

        if (request.getType() == Interview.InterviewType.PRESENTIEL &&
                (request.getLieu() == null || request.getLieu().trim().isEmpty())) {
            throw new InterviewValidationException("Le lieu est requis pour un entretien en présentiel");
        }
        
        if (request.getType() == Interview.InterviewType.VIDEO &&
                (request.getMeetingLink() == null || request.getMeetingLink().trim().isEmpty())) {
            throw new InterviewValidationException("Le lien de visio-conférence est requis pour un entretien en visio");
        }
        
        if (request.getDuree() == null || request.getDuree() <= 0) {
            throw new InterviewValidationException("La durée de l'entretien doit être supérieure à 0");
        }
    }

    private void checkScheduleConflict(LocalDateTime date, Integer duree, Long excludeId) {
        if (date == null || duree == null) return;
        
        LocalDateTime endTime = date.plusMinutes(duree);

        List<Interview> existingInterviews = interviewRepository.findByDateBetween(
                date.minusHours(2),
                endTime.plusHours(2)
        );

        for (Interview interview : existingInterviews) {
            if (excludeId != null && interview.getId().equals(excludeId)) {
                continue;
            }

            LocalDateTime interviewEnd = interview.getDateEntretien()
                    .plusMinutes(interview.getDureeMinutes());

            if (date.isBefore(interviewEnd) && endTime.isAfter(interview.getDateEntretien())) {
                throw new InterviewValidationException(
                    "Conflit d'horaire avec un autre entretien prévu le " +
                    interview.getDateEntretien().format(DATE_TIME_FORMATTER)
                );
            }
        }
    }

    private void envoyerEmailEntretien(Candidature candidature, Interview interview, String message) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                emailService.sendInterviewScheduledEmail(candidature, interview, message);
                log.info("✅ Email d'entretien envoyé avec succès");
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email d'entretien: {}", e.getMessage());
            }
        }).start();
    }

    private void envoyerEmailMiseAJour(Candidature candidature, Interview interview, String ancienneDate, String message) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                emailService.sendInterviewUpdatedEmail(candidature, interview, ancienneDate, message);
                log.info("✅ Email de mise à jour envoyé");
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email de mise à jour: {}", e.getMessage());
            }
        }).start();
    }

    private void envoyerEmailConfirmation(Candidature candidature, Interview interview, String message) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                emailService.sendInterviewScheduledEmail(candidature, interview, message);
                log.info("✅ Email de confirmation envoyé");
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email de confirmation: {}", e.getMessage());
            }
        }).start();
    }

    private void envoyerEmailAnnulation(Candidature candidature, Interview interview, String message) {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                emailService.sendInterviewCancelledEmail(candidature, interview, message);
                log.info("✅ Email d'annulation envoyé");
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email d'annulation: {}", e.getMessage());
            }
        }).start();
    }
 // com.example.recrutement.interview.service.InterviewService.java
 // Ajoutez ces méthodes

 // com.example.recrutement.interview.service.InterviewService.java
 // Ajoutez des logs pour debug

 public List<InterviewResponse> getInterviewsByCandidatEmail(String email) {
     log.info("📋 Récupération des entretiens pour candidat email: {}", email);
     
     User user = userRepository.findByEmail(email)
             .orElseThrow(() -> {
                 log.error("❌ Utilisateur non trouvé: {}", email);
                 return new InterviewValidationException("Utilisateur non trouvé");
             });
     
     log.info("👤 Utilisateur trouvé - ID: {}, Role: {}", user.getId(), user.getRole());
     
     if (user.getRole() != Role.CANDIDAT) {
         log.warn("⚠️ L'utilisateur {} n'est pas un candidat mais {}", email, user.getRole());
         throw new InterviewValidationException("L'utilisateur n'est pas un candidat");
     }
     
     // Récupérer les entretiens via les candidatures du candidat
     List<Interview> interviews = interviewRepository.findByCandidatId(user.getId());
     
     // Log détaillé
     log.info("📊 {} entretien(s) trouvé(s) pour le candidat {}", interviews.size(), email);
     
     if (!interviews.isEmpty()) {
         for (Interview interview : interviews) {
             log.info("   - Entretien ID: {}, Candidature ID: {}, Date: {}, Statut: {}", 
                      interview.getId(), 
                      interview.getCandidature().getId(),
                      interview.getDateEntretien(),
                      interview.getStatut());
         }
     } else {
         log.info("ℹ️ Aucun entretien trouvé pour le candidat {}", email);
         
         // Vérifier si le candidat a des candidatures
         List<Candidature> candidatures = candidatureRepository.findByCandidateId(user.getId());
         log.info("📊 Le candidat a {} candidature(s)", candidatures.size());
         
         if (candidatures.isEmpty()) {
             log.info("ℹ️ Le candidat n'a aucune candidature");
         } else {
             for (Candidature candidature : candidatures) {
                 log.info("   - Candidature ID: {}, Statut: {}", candidature.getId(), candidature.getStatut());
             }
         }
     }
     
     return interviews.stream()
             .map(InterviewResponse::fromEntity)
             .collect(Collectors.toList());
 }
 public InterviewResponse confirmInterviewByCandidate(Long id, String userEmail) {
     log.info("Confirmation de l'entretien par le candidat: {}", id);
     
     Interview interview = getInterviewEntityById(id);
     Candidature candidature = interview.getCandidature();
     
     // Vérifier que le candidat a le droit de confirmer cet entretien
     if (!candidature.getCandidate().getUser().getEmail().equals(userEmail)) {
         throw new InterviewValidationException("Vous n'avez pas le droit de confirmer cet entretien");
     }
     
     interview.setStatut(Interview.InterviewStatus.CONFIRME);
     Interview updated = interviewRepository.save(interview);
     
     // Publication d'événement
     try {
         eventPublisher.publishEvent(new InterviewEvent(
             this,
             interview.getId(),
             candidature.getId(),
             candidature.getCandidate().getPrenom() + " " + candidature.getCandidate().getNom(),
             candidature.getOffre().getTitre(),
             interview.getDateEntretien(),
             InterviewEvent.EventType.CONFIRMED
         ));
     } catch (Exception e) {
         log.error("Erreur publication événement: {}", e.getMessage());
     }
     
     // Envoyer email de confirmation
     envoyerEmailConfirmation(candidature, updated, "Votre entretien a été confirmé.");
     
     return InterviewResponse.fromEntity(updated);
 }


}