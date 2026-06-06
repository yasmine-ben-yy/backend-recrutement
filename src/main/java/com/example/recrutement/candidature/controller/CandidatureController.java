// com.example.recrutement.candidature.controller.CandidatureController.java
package com.example.recrutement.candidature.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.candidature.dto.*;
import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import com.example.recrutement.candidature.service.CandidatureService;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Candidatures", description = "API de gestion des candidatures")
@Slf4j
public class CandidatureController {

    private final CandidatureService service;
    private final CandidatureRepository candidatureRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    // ============================================================
    // ENDPOINTS POUR LES RH
    // ============================================================
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<CandidatureResponseDTO>> getAll(
            @RequestParam(required = false) Long candidatId,
            Authentication auth) {
        log.info("GET candidatures - user {}, candidatId={}", auth.getName(), candidatId);
        
        List<CandidatureResponseDTO> result;
        if (candidatId != null) {
            result = service.getByCandidate(candidatId);
        } else {
            result = service.getAllCandidatures();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CANDIDAT')")
    public ResponseEntity<CandidatureResponseDTO> postuler(
            @Valid @RequestBody CreateCandidatureDTO dto,
            Authentication auth) {
        log.info("POST candidature - user {}", auth.getName());
        return new ResponseEntity<>(service.postuler(dto), HttpStatus.CREATED);
    }

    @GetMapping("/offre/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<CandidatureResponseDTO>> getByOffre(
            @PathVariable UUID id,
            Authentication auth) {
        log.info("GET candidatures by offre {}", id);
        return ResponseEntity.ok(service.getByOffre(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<CandidatureResponseDTO> getById(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(service.getCandidatureById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<CandidatureResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Long rhId,
            @Valid @RequestBody UpdateStatusDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(
                service.updateStatus(id, dto.getStatut(), rhId, dto.getCommentaire())
        );
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<CandidatureResponseDTO> addNote(
            @PathVariable Long id,
            @RequestParam Long rhId,
            @Valid @RequestBody AddNoteDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(service.addNote(id, dto.getContenu(), rhId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication auth) {
        service.deleteCandidature(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/cv")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> downloadCV(@PathVariable Long id) {
        return downloadCVInternal(id, null);
    }

    // ============================================================
    // 🆕 ENDPOINTS POUR LE CANDIDAT CONNECTÉ
    // ============================================================

    /**
     * Récupère les candidatures du candidat connecté
     */
    @GetMapping("/mes-candidatures")
    @PreAuthorize("hasAuthority('ROLE_CANDIDAT')")
    @Operation(summary = "Récupérer mes candidatures")
    public ResponseEntity<List<CandidatureResponseDTO>> getMesCandidatures(Authentication auth) {
        String email = auth.getName();
        log.info("📋 GET mes candidatures - user: {}", email);
        
        // Récupérer l'utilisateur connecté
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé: {}", email);
                    return new RuntimeException("Utilisateur non trouvé avec l'email: " + email);
                });
        
        // Récupérer le profil candidat
        CandidateProfile candidat = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("Profil candidat non trouvé pour l'utilisateur: {}", email);
                    return new RuntimeException("Profil candidat non trouvé. Veuillez compléter votre profil.");
                });
        
        log.info("✅ Candidat trouvé - ID: {}, Nom: {} {}", candidat.getId(), candidat.getPrenom(), candidat.getNom());
        
        // Récupérer les candidatures
        List<CandidatureResponseDTO> candidatures = service.getByCandidate(candidat.getId());
        log.info("✅ {} candidature(s) trouvée(s)", candidatures.size());
        
        return ResponseEntity.ok(candidatures);
    }

    /**
     * Récupère une candidature spécifique du candidat connecté
     */
    @GetMapping("/mes-candidatures/{id}")
    @PreAuthorize("hasAuthority('ROLE_CANDIDAT')")
    @Operation(summary = "Récupérer une de mes candidatures")
    public ResponseEntity<CandidatureResponseDTO> getMaCandidature(
            @PathVariable Long id,
            Authentication auth) {
        
        String email = auth.getName();
        log.info("📋 GET ma candidature {} - user: {}", id, email);
        
        Candidature candidature = verifierProprietaireCandidature(id, email);
        
        return ResponseEntity.ok(service.getCandidatureById(id));
    }

    /**
     * Supprime une candidature du candidat connecté
     */
    @DeleteMapping("/mes-candidatures/{id}")
    @PreAuthorize("hasAuthority('ROLE_CANDIDAT')")
    @Operation(summary = "Supprimer une de mes candidatures")
    public ResponseEntity<Void> deleteMaCandidature(
            @PathVariable Long id,
            Authentication auth) {
        
        String email = auth.getName();
        log.info("🗑️ DELETE ma candidature {} - user: {}", id, email);
        
        verifierProprietaireCandidature(id, email);
        
        service.deleteCandidature(id);
        log.info("✅ Candidature {} supprimée avec succès", id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Télécharge le CV d'une candidature du candidat connecté
     */
    @GetMapping("/mes-candidatures/{id}/cv")
    @PreAuthorize("hasAuthority('ROLE_CANDIDAT')")
    @Operation(summary = "Télécharger le CV de ma candidature")
    public ResponseEntity<?> downloadMonCV(
            @PathVariable Long id,
            Authentication auth) {
        
        String email = auth.getName();
        log.info("📥 Téléchargement CV pour ma candidature ID: {} - user: {}", id, email);
        
        verifierProprietaireCandidature(id, email);
        
        return downloadCVInternal(id, email);
    }

    // ============================================================
    // MÉTHODES PRIVÉES UTILITAIRES
    // ============================================================

    /**
     * Vérifie que la candidature appartient bien au candidat connecté
     */
    private Candidature verifierProprietaireCandidature(Long candidatureId, String email) {
        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer le profil candidat
        CandidateProfile candidat = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profil candidat non trouvé"));
        
        // Récupérer la candidature
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + candidatureId));
        
        // Vérifier la propriété
        if (!candidature.getCandidate().getId().equals(candidat.getId())) {
            log.error("🚫 Tentative d'accès non autorisé - Candidature {} n'appartient pas au candidat {}", 
                     candidatureId, candidat.getId());
            throw new RuntimeException("Vous n'êtes pas autorisé à accéder à cette candidature");
        }
        
        return candidature;
    }

    /**
     * Logique interne de téléchargement de CV
     */
    private ResponseEntity<?> downloadCVInternal(Long id, String userEmail) {
        try {
            log.info("📥 Téléchargement du CV pour candidature ID: {}", id);
            
            Candidature candidature = candidatureRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec ID: " + id));
            
            String cvPath = null;
            
            // Vérifier d'abord dans la candidature
            if (candidature.getCvSnapshotPath() != null && !candidature.getCvSnapshotPath().isEmpty()) {
                cvPath = candidature.getCvSnapshotPath();
                log.info("✅ CV trouvé dans candidature: {}", cvPath);
            } 
            // Sinon vérifier dans le profil candidat
            else if (candidature.getCandidate() != null && candidature.getCandidate().getCvPrincipalPath() != null) {
                cvPath = candidature.getCandidate().getCvPrincipalPath();
                log.info("✅ CV trouvé dans profil candidat: {}", cvPath);
            }
            
            if (cvPath == null) {
                log.warn("⚠️ Aucun CV trouvé pour la candidature ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Aucun CV trouvé pour cette candidature"));
            }
            
            // Nettoyer le chemin
            String cleanPath = cvPath.startsWith("/") ? cvPath.substring(1) : cvPath;
            Path filePath = Paths.get(cleanPath);
            
            log.info("📁 Chemin complet du fichier: {}", filePath.toAbsolutePath());
            
            if (!Files.exists(filePath)) {
                log.error("❌ Fichier CV non trouvé: {}", filePath.toAbsolutePath());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Fichier CV introuvable sur le serveur"));
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/pdf";
            }
            
            String filename = filePath.getFileName().toString();
            
            log.info("✅ CV téléchargé avec succès: {}", filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("❌ Erreur téléchargement CV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du téléchargement du CV: " + e.getMessage()));
        }
    }
}