// com.example.recrutement.notification.repository.NotificationRepository.java
package com.example.recrutement.notification.repository;

import com.example.recrutement.notification.entity.Notification;
import com.example.recrutement.notification.entity.NotificationType;
import com.example.recrutement.notification.entity.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Récupérer les notifications d'un utilisateur
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Notification> findByUserIdAndIsReadFalseOrderByPriorityDescCreatedAtDesc(Long userId);
    
    // Filtrer par type
    Page<Notification> findByUserIdAndTypeInOrderByCreatedAtDesc(Long userId, List<NotificationType> types, Pageable pageable);
    
    // Non lues par type
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false GROUP BY n.type")
    List<Object[]> countUnreadByType(@Param("userId") Long userId);
    
    // Notifications urgentes
    List<Notification> findByUserIdAndPriorityInAndIsReadFalseOrderByCreatedAtDesc(
        Long userId, List<Priority> priorities);
    
    // Archiver les anciennes notifications
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isArchived = true, n.archivedAt = :archivedAt WHERE n.userId = :userId AND n.createdAt < :date")
    int archiveOldNotifications(@Param("userId") Long userId, @Param("date") LocalDateTime date, @Param("archivedAt") LocalDateTime archivedAt);
    
    // Marquer comme lues
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id IN :ids")
    int markAsRead(@Param("ids") List<Long> ids, @Param("readAt") LocalDateTime readAt);
    
    // Supprimer les notifications d'un utilisateur
    @Modifying
    @Transactional
    int deleteByUserIdAndIsArchivedTrue(Long userId);
    
    // Notifications expirées
    List<Notification> findByExpiresAtBeforeAndIsReadFalse(LocalDateTime now);
    
    // ✅ Vérifier si une notification similaire existe récemment (pour éviter les doublons)
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.userId = :userId AND n.type = :type " +
           "AND n.relatedEntityId = :relatedEntityId AND n.createdAt > :since")
    boolean existsByUserIdAndTypeAndRelatedEntityIdAndCreatedAtAfter(
        @Param("userId") Long userId,
        @Param("type") NotificationType type,
        @Param("relatedEntityId") Long relatedEntityId,
        @Param("since") LocalDateTime since);
    
    // Compter les notifications non lues par utilisateur
    long countByUserIdAndIsReadFalse(Long userId);
    
    // Compter les notifications urgentes non lues
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false " +
           "AND (n.priority = 'HIGH' OR n.priority = 'CRITICAL')")
    long countUrgentUnread(@Param("userId") Long userId);
    
    // Supprimer les notifications expirées
    @Modifying
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime now);
}