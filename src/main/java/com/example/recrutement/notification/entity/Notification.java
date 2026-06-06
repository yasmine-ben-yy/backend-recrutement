package com.example.recrutement.notification.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
 @Index(name = "idx_user_id", columnList = "user_id"),
 @Index(name = "idx_read", columnList = "is_read"),
 @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
 
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(nullable = false)
 private Long userId; // ID du RH destinataire
 
 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 private NotificationType type;
 
 @Column(nullable = false, columnDefinition = "TEXT")
 private String title;
 
 @Column(nullable = false, columnDefinition = "TEXT")
 private String content;
 
 @Column(nullable = false)
 private String actionUrl; // Lien vers la ressource concernée
 
 @Builder.Default
 private boolean isRead = false;
 
 @Builder.Default
 private boolean isArchived = false;
 
 @Enumerated(EnumType.STRING)
 private Priority priority; // LOW, MEDIUM, HIGH, URGENT
 
 private String relatedEntityType; // CANDIDATURE, INTERVIEW, OFFRE
 private Long relatedEntityId;
 
 private LocalDateTime readAt;
 private LocalDateTime archivedAt;
 
 @Builder.Default
 private LocalDateTime createdAt = LocalDateTime.now();
 private LocalDateTime expiresAt; // Date d'expiration de la notification
}