package com.example.recrutement.interview.controller;

import com.example.recrutement.interview.dto.EvaluationDTO;
import com.example.recrutement.interview.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> createEvaluation(
            @RequestParam Long interviewId,
            @RequestBody EvaluationDTO dto,
            @RequestParam Long evaluateurId) {
        try {
            log.info("=== POST /api/evaluations ===");
            log.info("interviewId: {}", interviewId);
            log.info("evaluateurId: {}", evaluateurId);
            log.info("dto: {}", dto);
            
            EvaluationDTO result = evaluationService.createEvaluation(interviewId, dto, evaluateurId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Erreur création évaluation: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "500");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> getEvaluation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(evaluationService.getEvaluation(id));
        } catch (Exception e) {
            log.error("❌ Erreur récupération évaluation: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @GetMapping("/interview/{interviewId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> getEvaluationsByInterview(@PathVariable Long interviewId) {
        try {
            log.info("📋 Récupération des évaluations pour l'entretien ID: {}", interviewId);
            List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByInterview(interviewId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("❌ Erreur récupération évaluations: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/candidat/{candidatId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> getEvaluationsByCandidat(@PathVariable Long candidatId) {
        try {
            return ResponseEntity.ok(evaluationService.getEvaluationsByCandidat(candidatId));
        } catch (Exception e) {
            log.error("❌ Erreur récupération évaluations: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/offre/{offreId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    public ResponseEntity<?> getEvaluationsByOffre(@PathVariable UUID offreId) {
        try {
            return ResponseEntity.ok(evaluationService.getEvaluationsByOffre(offreId));
        } catch (Exception e) {
            log.error("❌ Erreur récupération évaluations: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}