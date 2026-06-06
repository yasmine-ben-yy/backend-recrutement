package com.example.recrutement.candidate.controller;

import com.example.recrutement.candidate.dto.AIDetailedScoresDTO;
import com.example.recrutement.candidate.dto.CandidatMatchingDTO;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.service.AIMatchingService;
import com.example.recrutement.candidate.service.CandidateProfileService;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
public class AIMatchingController {

    private final AIMatchingService aiMatchingService;
    private final CandidateProfileService candidateProfileService;
    private final OffreRepository offreRepository;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "API de matching IA opérationnelle");
        return ResponseEntity.ok(response);
    }

    /**
     * RECOMMANDATIONS IA - UNIQUEMENT les candidats EXTERNES
     * (qui n'ont PAS encore postulé à cette offre)
     */
    @GetMapping("/recommandations/{offreId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<CandidatMatchingDTO>> getRecommendedCandidatesForOffre(
            @PathVariable UUID offreId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("🎯 Recommandations IA pour l'offre: {}", offreId);
        
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        // Récupérer les IDs des candidats qui ont déjà postulé
        List<Long> appliedCandidateIds = offre.getCandidatures().stream()
                .map(c -> c.getCandidate().getId())
                .collect(Collectors.toList());
        
        log.info("📋 Candidats ayant déjà postulé: {}", appliedCandidateIds);
        
        List<CandidateProfile> allCandidates = candidateProfileService.getAllCandidatsProfiles();
        
        // ✅ FILTRE IMPORTANT: Exclure les candidats qui ont déjà postulé
        List<CandidateProfile> externalCandidates = allCandidates.stream()
                .filter(c -> c.getCompetences() != null && !c.getCompetences().isEmpty())
                .filter(c -> !appliedCandidateIds.contains(c.getId()))  // ← Exclusion des candidats ayant postulé
                .collect(Collectors.toList());
        
        log.info("📊 Candidats externes avec compétences: {}/{}", 
                 externalCandidates.size(), allCandidates.size());
        
        List<CandidatMatchingDTO> results = externalCandidates.stream()
                .map(candidat -> {
                    double score = aiMatchingService.calculateMatchingScore(candidat, offre);
                    return new CandidatMatchingDTO(candidat, score);
                })
                .sorted((a, b) -> Double.compare(b.getMatchingScore(), a.getMatchingScore()))
                .limit(limit)
                .collect(Collectors.toList());
        
        log.info("✅ {} recommandations IA externes retournées", results.size());
        
        return ResponseEntity.ok(results);
    }

    /**
     * CANDIDATURES REÇUES - Uniquement les candidats ayant postulé
     */
    @GetMapping("/candidatures/{offreId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<CandidatMatchingDTO>> getAppliedCandidatesForOffre(
            @PathVariable UUID offreId) {
        
        log.info("📋 Candidatures reçues pour l'offre: {}", offreId);
        
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        List<CandidateProfile> appliedCandidates = offre.getCandidatures().stream()
                .map(c -> c.getCandidate())
                .filter(c -> c != null)
                .collect(Collectors.toList());
        
        log.info("📊 {} candidatures reçues", appliedCandidates.size());
        
        List<CandidatMatchingDTO> results = appliedCandidates.stream()
                .map(candidat -> {
                    double score = aiMatchingService.calculateMatchingScore(candidat, offre);
                    return new CandidatMatchingDTO(candidat, score);
                })
                .sorted((a, b) -> Double.compare(b.getMatchingScore(), a.getMatchingScore()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    /**
     * 🆕 Récupère les scores DÉTAILLÉS pour une candidature
     * Retourne: Global, Skills, Experience, Degree, Semantic, Title, Matched/Missing Skills
     */
    @GetMapping("/score-detaille")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<AIDetailedScoresDTO> getDetailedMatchingScore(
            @RequestParam Long candidateId,
            @RequestParam UUID offreId) {
        
        log.info("📊 [DÉTAILLÉ] Scores détaillés - Candidat: {}, Offre: {}", candidateId, offreId);
        
        CandidateProfile candidat = candidateProfileService.getCandidateProfileById(candidateId);
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        AIDetailedScoresDTO scores = aiMatchingService.getDetailedMatchingScores(candidat, offre);
        
        if (scores == null) {
            return ResponseEntity.internalServerError().build();
        }
        
        log.info("✅ Scores détaillés retournés: Global={}%, Skills={}%, Exp={}%, Degree={}%",
                scores.getGlobalScore(), scores.getSkillsScore(), 
                scores.getExperienceScore(), scores.getDegreeScore());
        
        return ResponseEntity.ok(scores);
    }
    @GetMapping("/score")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Map<String, Double>> getMatchingScore(
            @RequestParam Long candidateId,
            @RequestParam UUID offreId) {
        
        log.info("📊 Calcul du score - Candidat: {}, Offre: {}", candidateId, offreId);
        
        CandidateProfile candidat = candidateProfileService.getCandidateProfileById(candidateId);
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        double score = aiMatchingService.calculateMatchingScore(candidat, offre);
        
        Map<String, Double> response = new HashMap<>();
        response.put("matchingScore", score);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyse")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Map<String, Object>> getDetailedAnalysis(
            @RequestParam Long candidateId,
            @RequestParam UUID offreId) {
        
        log.info("🔍 Analyse détaillée - Candidat: {}, Offre: {}", candidateId, offreId);
        
        CandidateProfile candidat = candidateProfileService.getCandidateProfileById(candidateId);
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        Map<String, Object> analysis = aiMatchingService.getDetailedAnalysis(candidat, offre);
        return ResponseEntity.ok(analysis);
    }
}