package com.example.recrutement.candidate.controller;

import com.example.recrutement.candidate.dto.CandidateProfileDto;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.service.CandidateProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidat/profil")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profil Candidat", description = "Gestion du profil du candidat connecté")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @GetMapping
    @Operation(summary = "Récupérer le profil du candidat connecté")
    public ResponseEntity<CandidateProfileDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        log.info("Récupération du profil pour: {}", email);
        
        CandidateProfile profile = candidateProfileService.getByUserEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(convertToDto(profile));
    }

    @PutMapping
    @Operation(summary = "Créer ou mettre à jour le profil du candidat")
    public ResponseEntity<CandidateProfileDto> updateProfile(
            Authentication authentication,
            @RequestBody CandidateProfileDto dto) {
        
        String email = authentication.getName();
        log.info("Mise à jour du profil pour: {}", email);
        
        CandidateProfile profile = candidateProfileService.createOrUpdateByEmail(email, dto);
        return ResponseEntity.ok(convertToDto(profile));
    }

    // =============================================
    // GESTION DU CV
    // =============================================

    @PostMapping(value = "/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Uploader le CV du candidat")
    public ResponseEntity<Map<String, String>> uploadCV(
            Authentication authentication,
            @RequestParam("cv") MultipartFile file) {
        
        String email = authentication.getName();
        log.info("Upload CV pour: {}", email);
        
        if (file == null || file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Aucun fichier fourni");
            return ResponseEntity.badRequest().body(error);
        }
        
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && !contentType.equals("application/octet-stream"))) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Seuls les fichiers PDF sont acceptés");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (file.getSize() > 2 * 1024 * 1024) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le fichier ne doit pas dépasser 2MB");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            String userDir = "uploads/cvs/" + email;
            Path uploadPath = Paths.get(userDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Dossier créé: {}", uploadPath.toAbsolutePath());
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "cv_" + UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            
            Files.copy(file.getInputStream(), filePath);
            log.info("Fichier sauvegardé: {}", filePath.toAbsolutePath());
            
            String cvPath = "/" + userDir + "/" + filename;
            CandidateProfile profile = candidateProfileService.getByUserEmail(email);
            if (profile != null) {
                profile.setCvPrincipalPath(cvPath);
                candidateProfileService.createOrUpdateByEmail(email, convertToDto(profile));
                log.info("Profil mis à jour avec CV: {}", cvPath);
            } else {
                CandidateProfileDto newProfileDto = CandidateProfileDto.builder()
                        .cvPrincipalPath(cvPath)
                        .build();
                candidateProfileService.createOrUpdateByEmail(email, newProfileDto);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("url", cvPath);
            response.put("message", "CV uploadé avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Erreur upload CV: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de l'upload du CV: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // =============================================
    // GESTION DES COMPÉTENCES
    // =============================================

    @PostMapping("/competences")
    @Operation(summary = "Ajouter une compétence au profil")
    public ResponseEntity<Void> addCompetence(
            Authentication authentication,
            @RequestParam String competence) {
        
        String email = authentication.getName();
        log.info("Ajout de la compétence '{}' pour: {}", competence, email);
        
        CandidateProfile profile = candidateProfileService.getByUserEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        candidateProfileService.addCompetenceToProfile(profile.getId(), competence);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/competences/{competenceId}")
    @Operation(summary = "Supprimer une compétence du profil")
    public ResponseEntity<Void> removeCompetence(
            Authentication authentication,
            @PathVariable Long competenceId) {
        
        String email = authentication.getName();
        log.info("Suppression de la compétence ID: {} pour: {}", competenceId, email);
        
        CandidateProfile profile = candidateProfileService.getByUserEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        candidateProfileService.removeCompetenceFromProfile(profile.getId(), competenceId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/competences")
    @Operation(summary = "Mettre à jour toutes les compétences du profil")
    public ResponseEntity<Void> updateCompetences(
            Authentication authentication,
            @RequestBody List<String> competences) {
        
        String email = authentication.getName();
        log.info("Mise à jour des compétences pour: {}", email);
        
        CandidateProfile profile = candidateProfileService.getByUserEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        candidateProfileService.updateCompetences(profile.getId(), competences);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/competences")
    @Operation(summary = "Récupérer toutes les compétences du profil")
    public ResponseEntity<List<String>> getCompetences(Authentication authentication) {
        String email = authentication.getName();
        log.info("Récupération des compétences pour: {}", email);
        
        CandidateProfile profile = candidateProfileService.getByUserEmail(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<String> competences = profile.getCompetencesAsStrings();
        return ResponseEntity.ok(competences);
    }

    // =============================================
    // MÉTHODES UTILITAIRES
    // =============================================

    private CandidateProfileDto convertToDto(CandidateProfile profile) {
        return CandidateProfileDto.builder()
                .id(profile.getId())
                .nom(profile.getNom())
                .prenom(profile.getPrenom())
                .telephone(profile.getTelephone())
                .dateNaissance(profile.getDateNaissance())
                .ville(profile.getVille())
                .pays(profile.getPays())
                .titreProfessionnel(profile.getTitreProfessionnel())
                .experienceAnnees(profile.getExperienceAnnees())
                .niveauEtude(profile.getNiveauEtude())
                .disponibilite(profile.getDisponibilite())
                .competences(profile.getCompetencesAsStrings())
                .linkedinUrl(profile.getLinkedinUrl())
                .portfolioUrl(profile.getPortfolioUrl())
                .cvPrincipalPath(profile.getCvPrincipalPath())
                .lettreMotivationPath(profile.getLettreMotivationPath())
                .build();
    }
}