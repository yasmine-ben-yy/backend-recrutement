package com.example.recrutement.publics;

import com.example.recrutement.candidature.dto.CandidatureRapideRequest;
import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.service.CandidatureRapideService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/candidatures")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PublicCandidatureController {

    private final CandidatureRapideService candidatureRapideService;

    @PostMapping(value = "/rapide", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Postuler sans compte (candidature rapide)")
    public ResponseEntity<Map<String, Object>> postulerRapide(
            @ModelAttribute CandidatureRapideRequest request) {
        
        log.info("========================================");
        log.info("📝 NOUVELLE CANDIDATURE RAPIDE");
        log.info("========================================");
        log.info("📌 Offre ID: {}", request.getOffreId());
        log.info("👤 Nom: {}", request.getNom());
        log.info("👤 Prénom: {}", request.getPrenom());
        log.info("📧 Email: {}", request.getEmail());
        log.info("📞 Téléphone: {}", request.getTelephone());
        log.info("📎 CV reçu: {}", request.getCv() != null ? request.getCv().getOriginalFilename() : "Aucun");
        log.info("========================================");
        
        try {
            Candidature candidature = candidatureRapideService.postulerSansCompte(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature envoyée avec succès");
            response.put("candidatureId", candidature.getId());
            response.put("statut", candidature.getStatut().toString());
            response.put("matchingScore", candidature.getMatchingScore());
            
            log.info("✅ Candidature créée avec succès ! ID: {}", candidature.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la candidature rapide: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            
            if (e.getMessage() != null && e.getMessage().contains("déjà postulé")) {
                response.put("message", "Vous avez déjà postulé à cette offre avec cet email");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            if (e.getMessage() != null && e.getMessage().contains("Offre")) {
                response.put("message", "Offre non trouvée");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "L'API publique de candidature est opérationnelle");
        return ResponseEntity.ok(response);
    }
}