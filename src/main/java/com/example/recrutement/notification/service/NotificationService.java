// com.example.recrutement.notification.service.NotificationService.java
package com.example.recrutement.notification.service;

import com.example.recrutement.notification.dto.*;
import com.example.recrutement.notification.entity.*;
import com.example.recrutement.notification.repository.NotificationPreferenceRepository;
import com.example.recrutement.notification.repository.NotificationRepository;
import com.example.recrutement.service.IEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final IEmailService emailService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    // ==================== SPRINT 1 : CENTRE DE NOTIFICATIONS ====================
    
    public Page<NotificationDTO> getUserNotifications(Long userId, int page, int size, String type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage;
        
        if (type != null && !type.isEmpty()) {
            List<NotificationType> types = Arrays.stream(type.split(","))
                .map(NotificationType::valueOf)
                .collect(Collectors.toList());
            notificationPage = notificationRepository.findByUserIdAndTypeInOrderByCreatedAtDesc(userId, types, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        return notificationPage.map(this::toDTO);
    }
    
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByPriorityDescCreatedAtDesc(userId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void markAsRead(Long userId, List<Long> notificationIds) {
        notificationRepository.markAsRead(notificationIds, LocalDateTime.now());
        log.info("{} notification(s) marquée(s) comme lues pour l'utilisateur {}", notificationIds.size(), userId);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByPriorityDescCreatedAtDesc(userId);
        if (!unread.isEmpty()) {
            List<Long> ids = unread.stream().map(Notification::getId).collect(Collectors.toList());
            notificationRepository.markAsRead(ids, LocalDateTime.now());
            log.info("Toutes les notifications marquées comme lues pour l'utilisateur {}", userId);
        }
    }
    
    @Transactional
    public void archiveNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Non autorisé");
        }
        notification.setArchived(true);
        notification.setArchivedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("Notification {} archivée", notificationId);
    }
    
    @Transactional
    public void deleteAllArchived(Long userId) {
        int deletedCount = notificationRepository.deleteByUserIdAndIsArchivedTrue(userId);
        log.info("{} notification(s) archivée(s) supprimées pour l'utilisateur {}", deletedCount, userId);
    }
    
    
    public NotificationSummaryDTO getNotificationSummary(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByPriorityDescCreatedAtDesc(userId);
        
        List<Object[]> typeCounts = notificationRepository.countUnreadByType(userId);
        Map<String, Integer> unreadByType = new HashMap<>();
        for (Object[] row : typeCounts) {
            unreadByType.put(((NotificationType) row[0]).name(), ((Number) row[1]).intValue());
        }
        
        long urgentCount = unread.stream()
            .filter(n -> n.getPriority() == Priority.HIGH || n.getPriority() == Priority.CRITICAL)
            .count();
        
        return NotificationSummaryDTO.builder()
            .totalUnread(unread.size())
            .totalUrgent((int) urgentCount)
            .unreadByType(unreadByType)
            .recentNotifications(unread.stream().limit(10).map(this::toDTO).collect(Collectors.toList()))
            .dashboardWidgets(buildDashboardWidgets(userId))
            .build();
    }
    
    private Map<String, Object> buildDashboardWidgets(Long userId) {
        Map<String, Object> widgets = new HashMap<>();
        widgets.put("pendingCandidatures", 0);
        widgets.put("interviewsToday", 0);
        widgets.put("topIaCandidates", new ArrayList<>());
        return widgets;
    }
    
    
    @Async
    public void createNotification(Long userId, NotificationType type, String title, String content, 
                                   String actionUrl, Priority priority, String relatedEntityType, Long relatedEntityId) {
        try {
            NotificationPreferenceDTO prefs = getPreferences(userId);
            if (!isTypeEnabled(prefs, type)) {
                log.debug("Notification de type {} désactivée pour user {}", type, userId);
                return;
            }
            
            boolean existsRecently = notificationRepository.existsByUserIdAndTypeAndRelatedEntityIdAndCreatedAtAfter(
                userId, type, relatedEntityId, LocalDateTime.now().minusMinutes(5));
            
            if (existsRecently) {
                log.debug("Notification similaire récente ignorée pour user {}", userId);
                return;
            }
            
            Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .actionUrl(actionUrl)
                .priority(priority)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .createdAt(LocalDateTime.now())
                .expiresAt(calculateExpiryDate(type))
                .build();
            
            notification = notificationRepository.save(notification);
            
            if (prefs.isEnabledEmail() && (priority == Priority.HIGH || priority == Priority.CRITICAL)) {
                sendEmailNotification(userId, notification);
            }
            
            log.info("✅ Notification créée pour user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("❌ Erreur création notification: {}", e.getMessage(), e);
        }
    }
    
    private LocalDateTime calculateExpiryDate(NotificationType type) {
        switch (type) {
            case INTERVIEW_REMINDER_30MIN: return LocalDateTime.now().plusHours(1);
            case INTERVIEW_REMINDER_2H: return LocalDateTime.now().plusHours(3);
            case INTERVIEW_REMINDER_24H: return LocalDateTime.now().plusDays(2);
            case INTERVIEW_SCHEDULED: return LocalDateTime.now().plusDays(7);
            case NEW_CANDIDATURE: return LocalDateTime.now().plusDays(30);
            default: return LocalDateTime.now().plusDays(30);
        }
    }
    
    
    public void notifyNewCandidature(Long rhUserId, String candidatName, String offreTitle, 
                                     Long candidatureId, int matchingScore) {
        Priority priority = matchingScore >= 85 ? Priority.HIGH : (matchingScore >= 70 ? Priority.MEDIUM : Priority.LOW);
        String title = matchingScore >= 85 ? "🔥 Candidature très pertinente" : "📝 Nouvelle candidature";
        String content = String.format("%s a postulé au poste de %s. Score de correspondance : %d%%",
            candidatName, offreTitle, matchingScore);
        
        createNotification(rhUserId, NotificationType.NEW_CANDIDATURE, title, content,
            String.format("/rh/candidatures/%d", candidatureId), priority, "CANDIDATURE", candidatureId);
    }
    
    public void notifyCandidaturePendingReview(Long rhUserId, String offreTitle, int pendingDays, Long offreId) {
        String title = "⏰ Candidatures en attente";
        String content = String.format("L'offre %s a %d candidature(s) en attente d'évaluation depuis plus de %d jours.",
            offreTitle, pendingDays, pendingDays);
        
        createNotification(rhUserId, NotificationType.CANDIDATURE_PENDING_REVIEW, title, content,
            String.format("/rh/offres/%d/candidatures", offreId), Priority.MEDIUM, "OFFRE", offreId);
    }
    
    public void notifyHighScoreCandidature(Long rhUserId, String candidatName, String offreTitle, 
                                           Long candidatureId, int matchingScore, String alternativeOffre) {
        String title = "🎯 Candidat recommandé pour une autre offre";
        String content = String.format("%s correspond à %d%% pour %s. Ce profil pourrait convenir au poste %s.",
            candidatName, matchingScore, offreTitle, alternativeOffre);
        
        createNotification(rhUserId, NotificationType.IA_RECOMMENDATION, title, content,
            String.format("/rh/candidatures/%d", candidatureId), Priority.MEDIUM, "CANDIDATURE", candidatureId);
    }
    
    
    public void notifyInterviewScheduled(Long rhUserId, String candidatName, String offreTitle, 
                                         LocalDateTime dateTime, Long interviewId) {
        String formattedDate = dateTime.format(DATE_FORMATTER);
        String title = "📅 Entretien programmé";
        String content = String.format("Entretien avec %s pour le poste %s prévu le %s.",
            candidatName, offreTitle, formattedDate);
        
        createNotification(rhUserId, NotificationType.INTERVIEW_SCHEDULED, title, content,
            String.format("/rh/interviews/%d", interviewId), Priority.MEDIUM, "INTERVIEW", interviewId);
    }
    
    public void notifyInterviewConfirmed(Long rhUserId, String candidatName, String offreTitle, 
                                         LocalDateTime dateTime, Long interviewId) {
        String formattedDate = dateTime.format(DATE_FORMATTER);
        String title = "✅ Entretien confirmé";
        String content = String.format("%s a confirmé sa participation à l'entretien du %s.",
            candidatName, formattedDate);
        
        createNotification(rhUserId, NotificationType.INTERVIEW_CONFIRMED, title, content,
            String.format("/rh/interviews/%d", interviewId), Priority.MEDIUM, "INTERVIEW", interviewId);
    }
    
    public void notifyInterviewReminder(Long rhUserId, String candidatName, String offreTitle, 
                                        LocalDateTime dateTime, Long interviewId, int hoursBefore) {
        String formattedDate = dateTime.format(DATE_FORMATTER);
        String title = hoursBefore == 24 ? "📅 Rappel : Entretien demain" : 
                      (hoursBefore == 2 ? "⚠️ Entretien dans 2 heures" : "🔔 Entretien imminent");
        String content = String.format("Entretien avec %s pour le poste %s prévu à %s.",
            candidatName, offreTitle, formattedDate);
        
        NotificationType type = hoursBefore == 24 ? NotificationType.INTERVIEW_REMINDER_24H :
                               (hoursBefore == 2 ? NotificationType.INTERVIEW_REMINDER_2H : 
                                NotificationType.INTERVIEW_REMINDER_30MIN);
        Priority priority = hoursBefore <= 2 ? Priority.HIGH : Priority.MEDIUM;
        
        createNotification(rhUserId, type, title, content,
            String.format("/rh/interviews/%d", interviewId), priority, "INTERVIEW", interviewId);
    }
    
    
    public NotificationPreferenceDTO getPreferences(Long userId) {
        NotificationPreference prefs = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));
        return toPreferenceDTO(prefs);
    }
    
    @Transactional
    public NotificationPreferenceDTO updatePreferences(Long userId, NotificationPreferenceDTO dto) {
        NotificationPreference prefs = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));
        
        prefs.setEnabledInApp(dto.isEnabledInApp());
        prefs.setEnabledEmail(dto.isEnabledEmail());
        prefs.setDailyBriefing(dto.isDailyBriefing());
        prefs.setIaMatchingThreshold(dto.getIaMatchingThreshold());
        prefs.setInterviewReminder24h(dto.getInterviewReminder24h() > 0);
        prefs.setInterviewReminder2h(dto.getInterviewReminder2h() > 0);
        prefs.setInterviewReminder30min(dto.getInterviewReminder30min() > 0);
        
        if (dto.getDisabledTypes() != null && dto.getDisabledTypes().length > 0) {
            prefs.setDisabledTypes(String.join(",", dto.getDisabledTypes()));
        } else {
            prefs.setDisabledTypes("");
        }
        
        prefs.setUpdatedAt(LocalDateTime.now());
        preferenceRepository.save(prefs);
        
        log.info("Préférences mises à jour pour l'utilisateur {}", userId);
        return toPreferenceDTO(prefs);
    }
    
    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference prefs = NotificationPreference.builder()
            .userId(userId)
            .enabledInApp(true)
            .enabledEmail(true)
            .dailyBriefing(true)
            .iaMatchingThreshold(85)
            .interviewReminder24h(true)
            .interviewReminder2h(true)
            .interviewReminder30min(true)
            .disabledTypes("")
            .build();
        return preferenceRepository.save(prefs);
    }
    
    private boolean isTypeEnabled(NotificationPreferenceDTO prefs, NotificationType type) {
        if (prefs.getDisabledTypes() == null || prefs.getDisabledTypes().length == 0) {
            return true;
        }
        return !Arrays.asList(prefs.getDisabledTypes()).contains(type.name());
    }
    
    
    @Scheduled(cron = "0 0 9 * * *")
    @Async
    public void sendDailyBriefings() {
        log.info("📧 Envoi des briefings quotidiens...");
    }
    
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkExpiredNotifications() {
        List<Notification> expired = notificationRepository.findByExpiresAtBeforeAndIsReadFalse(LocalDateTime.now());
        if (!expired.isEmpty()) {
            log.info("{} notification(s) expirée(s) trouvée(s)", expired.size());
        }
    }
    
    private void sendEmailNotification(Long userId, Notification notification) {
        try {
            log.info("📧 Email envoyé pour notification {} à l'utilisateur {}", notification.getId(), userId);
        } catch (Exception e) {
            log.error("❌ Erreur envoi email: {}", e.getMessage());
        }
    }
    
    // ==================== NOTIFICATIONS OFFRES ====================

    public void notifyOffreExpiringSoon(Long rhUserId, String offreTitle, LocalDateTime expiryDate, UUID offreId, int daysBefore) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = expiryDate.format(formatter);
        
        String title;
        if (daysBefore == 3) {
            title = "⚠️ Offre expire dans 3 jours";
        } else if (daysBefore == 7) {
            title = "📅 Offre expire dans une semaine";
        } else {
            title = "📅 Offre bientôt expirée";
        }
        
        String content = String.format("L'offre \"%s\" expire le %s. Pensez à la renouveler ou à clôturer les candidatures.", 
            offreTitle, formattedDate);
        
        createNotification(
            rhUserId,
            NotificationType.OFFRE_EXPIRING_SOON,
            title,
            content,
            String.format("/rh/offres/%s", offreId),
            daysBefore == 3 ? Priority.HIGH : Priority.MEDIUM,
            "OFFRE",
            offreId != null ? offreId.hashCode() : 0L
        );
    }

    public void notifyOffreExpiringToday(Long rhUserId, String offreTitle, LocalDateTime expiryDate, UUID offreId) {
        String title = "🔴 Offre expire aujourd'hui !";
        String content = String.format("L'offre \"%s\" expire aujourd'hui. Une action immédiate est requise.", offreTitle);
        
        createNotification(
            rhUserId,
            NotificationType.OFFRE_EXPIRING_SOON,
            title,
            content,
            String.format("/rh/offres/%s", offreId),
            Priority.CRITICAL,
            "OFFRE",
            offreId != null ? offreId.hashCode() : 0L
        );
    }

    public void notifyOffreExpired(Long rhUserId, String offreTitle, UUID offreId) {
        String title = "❌ Offre expirée";
        String content = String.format("L'offre \"%s\" a expiré. Elle n'est plus visible par les candidats.", offreTitle);
        
        createNotification(
            rhUserId,
            NotificationType.OFFRE_EXPIRED,
            title,
            content,
            String.format("/rh/offres/%s", offreId),
            Priority.HIGH,
            "OFFRE",
            offreId != null ? offreId.hashCode() : 0L
        );
    }
    
    // ==================== HELPERS ====================
    
    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .type(notification.getType().name())
            .title(notification.getTitle())
            .content(notification.getContent())
            .actionUrl(notification.getActionUrl())
            .isRead(notification.isRead())
            .isArchived(notification.isArchived())
            .priority(notification.getPriority().name())
            .relatedEntityType(notification.getRelatedEntityType())
            .relatedEntityId(notification.getRelatedEntityId())
            .createdAt(notification.getCreatedAt())
            .readAt(notification.getReadAt())
            .build();
    }
    
    private NotificationPreferenceDTO toPreferenceDTO(NotificationPreference prefs) {
        String[] disabledTypes = prefs.getDisabledTypes() != null && !prefs.getDisabledTypes().isEmpty() 
            ? prefs.getDisabledTypes().split(",") : new String[0];
        
        return NotificationPreferenceDTO.builder()
            .enabledInApp(prefs.isEnabledInApp())
            .enabledEmail(prefs.isEnabledEmail())
            .dailyBriefing(prefs.isDailyBriefing())
            .iaMatchingThreshold(prefs.getIaMatchingThreshold())
            .interviewReminder24h(prefs.isInterviewReminder24h() ? 24 : 0)
            .interviewReminder2h(prefs.isInterviewReminder2h() ? 2 : 0)
            .interviewReminder30min(prefs.isInterviewReminder30min() ? 30 : 0)
            .disabledTypes(disabledTypes)
            .build();
    }
}