// com.example.recrutement.notification.entity.NotificationType.java
package com.example.recrutement.notification.entity;

public enum NotificationType {
    // Candidatures
    NEW_CANDIDATURE,
    HIGH_SCORE_CANDIDATURE,
    CANDIDATURE_PENDING_REVIEW,
    CANDIDATURE_EXPIRING_SOON,
    CANDIDATURE_STATUS_CHANGED,
    
    // Entretiens
    INTERVIEW_SCHEDULED,
    INTERVIEW_REMINDER_24H,
    INTERVIEW_REMINDER_2H,
    INTERVIEW_REMINDER_30MIN,
    INTERVIEW_CONFIRMED,
    INTERVIEW_CANCELLED,
    INTERVIEW_RESCHEDULED,
    INTERVIEW_PENDING_EVALUATION,
    
    // Offres
    OFFRE_EXPIRING_SOON,
    OFFRE_NO_CANDIDATE,
    OFFRE_STATS_UPDATE,
    
    // IA / Matching
    IA_TOP_CANDIDATE_DETECTED,
    IA_SCORE_DROPPED,
    IA_RECOMMENDATION,
    
    // Collaboratif
    COMMENT_ADDED,
    RH_ASSIGNED,
    MANAGER_FEEDBACK,
    
    // Pipeline
    PIPELINE_STUCK,
    STEP_DELAY_WARNING,
    
    // Système
    SYSTEM_ALERT,
    WEEKLY_DIGEST,
    DAILY_BRIEFING, OFFRE_EXPIRED
}