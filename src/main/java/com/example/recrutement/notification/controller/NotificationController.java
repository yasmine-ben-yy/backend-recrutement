// com.example.recrutement.notification.controller.NotificationController.java
package com.example.recrutement.notification.controller;

import com.example.recrutement.notification.dto.*;
import com.example.recrutement.notification.service.NotificationService;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rh/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications RH", description = "Gestion des notifications pour les recruteurs. Permet de consulter, marquer comme lues, archiver et configurer les notifications.")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // ==================== GET NOTIFICATIONS ====================
    
    @GetMapping
    @Operation(
        summary = "Récupérer les notifications paginées",
        description = "Retourne la liste des notifications de l'utilisateur RH connecté avec pagination. Permet de filtrer par type."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filtrer par type de notification", example = "NEW_CANDIDATURE")
            @RequestParam(required = false) String type) {
        Long userId = getUserIdFromDetails(user);
        log.info("📋 getNotifications - userId: {}", userId);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, page, size, type));
    }
    
    // ==================== GET UNREAD NOTIFICATIONS ====================
    
    @GetMapping("/unread")
    @Operation(
        summary = "Récupérer les notifications non lues",
        description = "Retourne toutes les notifications non lues de l'utilisateur RH connecté, triées par priorité décroissante."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications non lues récupérées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromDetails(user);
        log.info("📋 getUnreadNotifications - userId: {}", userId);
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        log.info("📊 {} notifications non lues trouvées", notifications.size());
        return ResponseEntity.ok(notifications);
    }
    
    // ==================== GET SUMMARY ====================
    
    @GetMapping("/summary")
    @Operation(
        summary = "Récupérer le résumé des notifications",
        description = "Retourne un résumé statistique des notifications : nombre total non lues, nombre urgentes, répartition par type."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résumé récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<NotificationSummaryDTO> getNotificationSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromDetails(user);
        log.info("📊 getNotificationSummary - userId: {}", userId);
        return ResponseEntity.ok(notificationService.getNotificationSummary(userId));
    }
    
    // ==================== MARK AS READ ====================
    
    @PutMapping("/read")
    @Operation(
        summary = "Marquer des notifications comme lues",
        description = "Marque une liste de notifications comme lues pour l'utilisateur connecté."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications marquées comme lues"),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Liste des IDs des notifications à marquer comme lues",
                required = true,
                content = @Content(examples = @ExampleObject(value = "[1, 2, 3]"))
            )
            @RequestBody List<Long> notificationIds) {
        Long userId = getUserIdFromDetails(user);
        notificationService.markAsRead(userId, notificationIds);
        return ResponseEntity.ok().build();
    }
    
    // ==================== MARK ALL AS READ ====================
    
    @PutMapping("/read/all")
    @Operation(
        summary = "Marquer toutes les notifications comme lues",
        description = "Marque toutes les notifications non lues de l'utilisateur comme lues."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Toutes les notifications ont été marquées comme lues"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromDetails(user);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    // ==================== ARCHIVE NOTIFICATION ====================
    
    @DeleteMapping("/{notificationId}")
    @Operation(
        summary = "Archiver une notification",
        description = "Archive une notification spécifique pour l'utilisateur connecté."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification archivée avec succès"),
        @ApiResponse(responseCode = "404", description = "Notification non trouvée"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<Void> archiveNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
            @Parameter(description = "ID de la notification à archiver", required = true, example = "1")
            @PathVariable Long notificationId) {
        Long userId = getUserIdFromDetails(user);
        notificationService.archiveNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }
    
    // ==================== DELETE ALL ARCHIVED ====================
    
    @DeleteMapping("/archived")
    @Operation(
        summary = "Supprimer toutes les notifications archivées",
        description = "Supprime définitivement toutes les notifications archivées de l'utilisateur."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications archivées supprimées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<Void> deleteAllArchived(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromDetails(user);
        notificationService.deleteAllArchived(userId);
        return ResponseEntity.ok().build();
    }
    
    // ==================== GET PREFERENCES ====================
    
    @GetMapping("/preferences")
    @Operation(
        summary = "Récupérer les préférences de notification",
        description = "Retourne les préférences de notification de l'utilisateur (emails, rappels, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Préférences récupérées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<NotificationPreferenceDTO> getPreferences(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromDetails(user);
        return ResponseEntity.ok(notificationService.getPreferences(userId));
    }
    
    // ==================== UPDATE PREFERENCES ====================
    
    @PutMapping("/preferences")
    @Operation(
        summary = "Mettre à jour les préférences de notification",
        description = "Met à jour les préférences de notification de l'utilisateur."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Préférences mises à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<NotificationPreferenceDTO> updatePreferences(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Nouvelles préférences de notification",
                required = true,
                content = @Content(schema = @Schema(implementation = NotificationPreferenceDTO.class))
            )
            @RequestBody NotificationPreferenceDTO preferences) {
        Long userId = getUserIdFromDetails(user);
        return ResponseEntity.ok(notificationService.updatePreferences(userId, preferences));
    }
    
    // ==================== TEST ENDPOINT (pour développement) ====================
    
    @PostMapping("/test/create")
    @Operation(
        summary = "Créer une notification de test",
        description = "Endpoint temporaire pour créer une notification de test (utile pour le développement)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification de test créée"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<String> createTestNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromDetails(user);
        
        notificationService.createNotification(
            userId,
            com.example.recrutement.notification.entity.NotificationType.NEW_CANDIDATURE,
            "📝 Notification de test",
            "Ceci est une notification de test créée depuis Swagger",
            "/dashboard",
            com.example.recrutement.notification.entity.Priority.MEDIUM,
            "TEST",
            1L
        );
        
        return ResponseEntity.ok("✅ Notification de test créée avec succès");
    }
    
    // ==================== HELPER METHOD ====================
    
    /**
     * Extrait l'ID de l'utilisateur à partir des détails d'authentification
     * @param user UserDetails de Spring Security
     * @return ID de l'utilisateur ou null si non trouvé
     */
    private Long getUserIdFromDetails(UserDetails user) {
        if (user == null) {
            log.error("❌ UserDetails est null");
            return null;
        }
        
        String email = user.getUsername();
        log.info("🔍 Recherche de l'utilisateur avec email: {}", email);
        
        return userRepository.findByEmail(email)
            .map(u -> {
                log.info("✅ Utilisateur trouvé - ID: {}, Email: {}, Role: {}", 
                         u.getId(), u.getEmail(), u.getRole());
                return u.getId();
            })
            .orElseGet(() -> {
                log.error("❌ Aucun utilisateur trouvé avec email: {}", email);
                return null;
            });
    }
}