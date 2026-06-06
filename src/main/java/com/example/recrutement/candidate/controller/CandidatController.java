package com.example.recrutement.candidate.controller;

import com.example.recrutement.candidate.dto.CandidatDetailResponseDTO;
import com.example.recrutement.candidate.dto.CandidatListResponseDTO;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.service.CandidateProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
@Slf4j
public class CandidatController {

    private final CandidateProfileService candidateService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<CandidatListResponseDTO>> getAllCandidats() {
        log.info("Récupération de tous les candidats pour les RH");
        List<CandidatListResponseDTO> candidats = candidateService.getAllCandidats();
        return ResponseEntity.ok(candidats);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<CandidatDetailResponseDTO> getCandidatById(@PathVariable Long id) {
        log.info("Récupération du détail du candidat avec ID: {}", id);
        CandidatDetailResponseDTO candidat = candidateService.getCandidatDetailById(id);
        return ResponseEntity.ok(candidat);
    }
    
    @GetMapping("/{id}/cv")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Resource> downloadCV(@PathVariable Long id) {
        try {
            log.info("📥 Téléchargement du CV pour candidat ID: {}", id);
            
            CandidateProfile profile = candidateService.getCandidateProfileById(id);
            if (profile == null) {
                log.warn("Candidat non trouvé pour ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            String cvPath = profile.getCvPrincipalPath();
            if (cvPath == null || cvPath.isEmpty()) {
                log.warn("Aucun CV trouvé pour candidat ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            String cleanPath = cvPath.startsWith("/") ? cvPath.substring(1) : cvPath;
            Path filePath = Paths.get(cleanPath);
            
            log.info("Chemin du fichier: {}", filePath.toAbsolutePath());
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() && !resource.isReadable()) {
                log.error("Fichier CV non trouvé: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            String filename = "CV_" + profile.getPrenom() + "_" + profile.getNom() + ".pdf";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Erreur téléchargement CV: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/recherche/competence")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<CandidatListResponseDTO>> searchByCompetence(
            @RequestParam String competence) {
        log.info("Recherche de candidats par compétence: {}", competence);
        List<CandidatListResponseDTO> candidats = candidateService.searchCandidatsByCompetence(competence);
        return ResponseEntity.ok(candidats);
    }

    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("Récupération des statistiques des candidats");
        Map<String, Object> stats = candidateService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}