package com.example.recrutement.ai.service;

import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AIEmbeddingService {

    @Value("${ai.embedding.url:http://localhost:8000/match}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Double> scoreCache = new ConcurrentHashMap<>();

    public AIEmbeddingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        // Configuration des timeouts
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(60000);
        restTemplate.setRequestFactory(factory);
    }

    /**
     * Construit le texte du candidat pour l'embedding
     */
    private String buildCandidateText(CandidateProfile candidat) {
        StringBuilder sb = new StringBuilder();
        
        if (candidat.getTitreProfessionnel() != null && !candidat.getTitreProfessionnel().isEmpty()) {
            sb.append(candidat.getTitreProfessionnel()).append(". ");
        }
        
        if (candidat.getCompetences() != null && !candidat.getCompetences().isEmpty()) {
            sb.append("Compétences: ");
            sb.append(String.join(", ", candidat.getCompetencesAsStrings()));
            sb.append(". ");
        }
        
        if (candidat.getExperienceAnnees() != null && candidat.getExperienceAnnees() > 0) {
            sb.append(candidat.getExperienceAnnees()).append(" ans d'expérience. ");
        }
        
        if (candidat.getNiveauEtude() != null && !candidat.getNiveauEtude().isEmpty()) {
            sb.append("Diplôme: ").append(candidat.getNiveauEtude()).append(". ");
        }
        
        return sb.toString();
    }

    /**
     * Construit le texte de l'offre pour l'embedding
     */
    private String buildJobText(OffreEmploi offre) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(offre.getTitre()).append(". ");
        
        if (offre.getCompetencesRequises() != null && !offre.getCompetencesRequises().isEmpty()) {
            sb.append("Compétences requises: ");
            sb.append(String.join(", ", offre.getCompetencesRequises()));
            sb.append(". ");
        }
        
        if (offre.getExperienceRequise() != null && offre.getExperienceRequise() > 0) {
            sb.append(offre.getExperienceRequise()).append(" ans d'expérience requis. ");
        }
        
        if (offre.getNiveauEtude() != null && !offre.getNiveauEtude().isEmpty()) {
            sb.append("Diplôme requis: ").append(offre.getNiveauEtude()).append(". ");
        }
        
        return sb.toString();
    }

    /**
     * Calcule le score de matching avec l'API Python (embeddings)
     */
    public Double calculateMatchingScore(CandidateProfile candidat, OffreEmploi offre) {
        String cacheKey = candidat.getId() + "_" + offre.getId();
        
        if (scoreCache.containsKey(cacheKey)) {
            log.info("📦 Cache hit - Score: {}%", scoreCache.get(cacheKey));
            return scoreCache.get(cacheKey);
        }
        
        String candidateText = buildCandidateText(candidat);
        String jobText = buildJobText(offre);
        
        log.debug("📝 Texte candidat: {}", candidateText);
        log.debug("📝 Texte offre: {}", jobText);
        
        try {
            Map<String, String> request = new HashMap<>();
            request.put("candidate_text", candidateText);
            request.put("job_text", jobText);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, Map.class
            );
            
            if (response.getBody() != null && response.getBody().containsKey("score")) {
                Object scoreObj = response.getBody().get("score");
                Double score = scoreObj instanceof Number ? 
                    ((Number) scoreObj).doubleValue() : 
                    Double.parseDouble(scoreObj.toString());
                
                scoreCache.put(cacheKey, score);
                log.info("🤖 Score IA embedding: {}%", score);
                return score;
            }
            
        } catch (Exception e) {
            log.error("❌ Erreur appel service embedding: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Vérifie si le service IA est disponible
     */
    public boolean isAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8000/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("⚠️ Service embedding non disponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vide le cache
     */
    public void clearCache() {
        scoreCache.clear();
        log.info("🗑️ Cache Spring vidé");
        
        // Vider le cache Python
        try {
            restTemplate.delete("http://localhost:8000/cache");
            log.info("🗑️ Cache Python vidé");
        } catch (Exception e) {
            log.warn("Impossible de vider le cache Python");
        }
    }
}