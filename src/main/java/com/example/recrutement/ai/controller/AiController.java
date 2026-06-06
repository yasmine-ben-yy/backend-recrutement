// com.example.recrutement.ai.controller/AiController.java
package com.example.recrutement.ai.controller;

import com.example.recrutement.ai.service.AiInterviewService;
import com.example.recrutement.interview.dto.AiQuestionResponseDTO;
import com.example.recrutement.interview.entity.AiAnalysis;
import com.example.recrutement.interview.entity.AiSummary;
import com.example.recrutement.interview.entity.InterviewQuestion;
import com.example.recrutement.interview.service.AiDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "IA - Intelligence Artificielle", description = "Endpoints pour la gestion des fonctionnalités IA")
public class AiController {

    private final AiInterviewService aiInterviewService;
    private final AiDataService aiDataService;
    private final RestTemplate restTemplate = new RestTemplate();

    // ============================================================
    // ENDPOINTS DE GÉNÉRATION INTERNE
    // ============================================================

    @PostMapping("/questions/{interviewId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<InterviewQuestion>> generateQuestions(@PathVariable Long interviewId) {
        return ResponseEntity.ok(aiInterviewService.generateInterviewQuestions(interviewId));
    }

    @GetMapping("/summary/{candidatId}/{offreId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<String> getSummary(
            @PathVariable Long candidatId,
            @PathVariable UUID offreId) {
        return ResponseEntity.ok(aiInterviewService.generateCandidateSummary(candidatId, offreId));
    }

    // ✅ UNE SEULE MÉTHODE POUR /summary/{candidatureId}
    @GetMapping("/summary/{candidatureId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Map<String, Object>> getSummaryByCandidature(@PathVariable Long candidatureId) {
        log.info("📋 Récupération du résumé IA pour candidature: {}", candidatureId);
        AiSummary summary = aiDataService.getSummaryByCandidature(candidatureId);
        
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Construire une réponse simple sans les relations complexes
        Map<String, Object> response = new HashMap<>();
        response.put("id", summary.getId());
        response.put("summary", summary.getSummary());
        response.put("mainSkills", summary.getMainSkills());
        response.put("experienceYears", summary.getExperienceYears());
        response.put("compatibilityScore", summary.getCompatibilityScore());
        response.put("recommendations", summary.getRecommendations());
        response.put("candidateLevel", summary.getCandidateLevel());
        response.put("createdAt", summary.getCreatedAt());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyze/{candidatId}/{offreId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Map<String, Object>> analyzeCV(
            @PathVariable Long candidatId,
            @PathVariable UUID offreId) {
        return ResponseEntity.ok(aiInterviewService.analyzeCV(candidatId, offreId));
    }

    // ============================================================
    // PROXY VERS FASTAPI
    // ============================================================

    @PostMapping("/generate-questions")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> proxyGenerateQuestions(@RequestBody Map<String, Object> request) {
        try {
            log.info("📤 Proxy vers FastAPI: /ai/generate-questions");
            var response = restTemplate.postForEntity("http://localhost:8000/ai/generate-questions", request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("❌ Erreur proxy: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Service IA indisponible"));
        }
    }

    @PostMapping("/summary-proxy")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> proxySummary(@RequestBody Map<String, String> request) {
        try {
            var response = restTemplate.postForEntity("http://localhost:8000/ai/summary", request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Service IA indisponible"));
        }
    }

    @PostMapping("/analyze-proxy")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> proxyAnalyze(@RequestBody Map<String, String> request) {
        try {
            var response = restTemplate.postForEntity("http://localhost:8000/ai/analyze", request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Service IA indisponible"));
        }
    }

    // ============================================================
    // SAUVEGARDE DES DONNÉES IA
    // ============================================================

    @PostMapping("/save-questions")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> saveQuestions(@RequestBody Map<String, Object> request) {
        log.info("📝 Sauvegarde des questions IA");
        
        Long interviewId = Long.valueOf(request.get("interviewId").toString());
        @SuppressWarnings("unchecked")
        List<String> questions = (List<String>) request.get("questions");
        String candidateLevel = (String) request.get("candidateLevel");
        
        List<InterviewQuestion> saved = aiDataService.saveQuestions(interviewId, questions, candidateLevel);
        
        if (saved.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Aucune question sauvegardée"));
        }
        
        AiQuestionResponseDTO response = AiQuestionResponseDTO.builder()
                .id(saved.get(0).getId())
                .interviewId(interviewId)
                .questions(questions)
                .candidateLevel(candidateLevel)
                .questionCount(questions.size())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save-summary")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Map<String, Object>> saveSummary(@RequestBody Map<String, Object> request) {
        log.info("📝 Sauvegarde du résumé IA");
        
        Long candidatureId = Long.valueOf(request.get("candidatureId").toString());
        String summary = (String) request.get("summary");
        @SuppressWarnings("unchecked")
        List<String> mainSkills = (List<String>) request.get("mainSkills");
        Integer experienceYears = (Integer) request.get("experienceYears");
        Integer compatibilityScore = (Integer) request.get("compatibilityScore");
        @SuppressWarnings("unchecked")
        List<String> recommendations = (List<String>) request.get("recommendations");
        String candidateLevel = (String) request.get("candidateLevel");
        
        AiSummary saved = aiDataService.saveSummary(candidatureId, summary, mainSkills, 
                experienceYears, compatibilityScore, recommendations, candidateLevel);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("summary", saved.getSummary());
        response.put("mainSkills", saved.getMainSkills());
        response.put("experienceYears", saved.getExperienceYears());
        response.put("compatibilityScore", saved.getCompatibilityScore());
        response.put("recommendations", saved.getRecommendations());
        response.put("candidateLevel", saved.getCandidateLevel());
        response.put("createdAt", saved.getCreatedAt());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions/{interviewId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<List<InterviewQuestion>> getQuestionsByInterview(@PathVariable Long interviewId) {
        log.info("📋 Récupération des questions IA pour l'entretien: {}", interviewId);
        List<InterviewQuestion> questions = aiDataService.getQuestionsByInterview(interviewId);
        return ResponseEntity.ok(questions);
    }
    @GetMapping("/analysis/{candidatureId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<Map<String, Object>> getAnalysisByCandidature(@PathVariable Long candidatureId) {
        log.info("📋 Récupération de l'analyse IA pour candidature: {}", candidatureId);
        AiAnalysis analysis = aiDataService.getAnalysisByCandidature(candidatureId);
        
        if (analysis == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", analysis.getId());
        response.put("matchedSkills", analysis.getMatchedSkills());
        response.put("missingSkills", analysis.getMissingSkills());
        response.put("semanticScore", analysis.getSemanticScore());
        response.put("recommendation", analysis.getRecommendation());
        response.put("strengths", analysis.getStrengths());
        response.put("weaknesses", analysis.getWeaknesses());
        response.put("recommendationsList", analysis.getRecommendationsList());
        response.put("confidenceScore", analysis.getConfidenceScore());
        response.put("createdAt", analysis.getCreatedAt());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save-analysis")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<AiAnalysis> saveAnalysis(@RequestBody Map<String, Object> request) {
        log.info("📝 Sauvegarde de l'analyse IA");
        
        Long candidatureId = Long.valueOf(request.get("candidatureId").toString());
        
        String offreIdStr = request.get("offreId").toString();
        UUID offreId = UUID.fromString(offreIdStr);
        
        @SuppressWarnings("unchecked")
        List<String> matchedSkills = (List<String>) request.get("matchedSkills");
        @SuppressWarnings("unchecked")
        List<String> missingSkills = (List<String>) request.get("missingSkills");
        Integer semanticScore = (Integer) request.get("semanticScore");
        String recommendation = (String) request.get("recommendation");
        @SuppressWarnings("unchecked")
        List<String> strengths = (List<String>) request.get("strengths");
        @SuppressWarnings("unchecked")
        List<String> weaknesses = (List<String>) request.get("weaknesses");
        @SuppressWarnings("unchecked")
        List<String> recommendationsList = (List<String>) request.get("recommendationsList");
        Integer confidenceScore = (Integer) request.get("confidenceScore");
        
        AiAnalysis saved = aiDataService.saveAnalysis(candidatureId, offreId, matchedSkills,
                missingSkills, semanticScore, recommendation, strengths, weaknesses,
                recommendationsList, confidenceScore);
        
        return ResponseEntity.ok(saved);
    }
}