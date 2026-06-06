// com.example.recrutement.notification.entity.NotificationPreference.java
package com.example.recrutement.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    // Notifications in-app
    @Builder.Default
    private boolean enabledInApp = true;
    
    // Notifications email
    @Builder.Default
    private boolean enabledEmail = true;
    
    // Briefing quotidien
    @Builder.Default
    private boolean dailyBriefing = true;
    
    // Score IA minimum pour alerte
    @Builder.Default
    private int iaMatchingThreshold = 85;
    
    // Délais rappels entretien
    @Builder.Default
    private boolean interviewReminder24h = true;
    
    @Builder.Default
    private boolean interviewReminder2h = true;
    
    @Builder.Default
    private boolean interviewReminder30min = true;
    
    // Types de notifications activés/désactivés (séparés par virgules)
    @Column(length = 1000)
    private String disabledTypes;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}