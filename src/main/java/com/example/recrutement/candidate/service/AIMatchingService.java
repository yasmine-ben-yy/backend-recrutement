package com.example.recrutement.candidate.service;

import com.example.recrutement.candidate.dto.AIDetailedScoresDTO;
import com.example.recrutement.candidate.dto.PythonMatchResponse;
import com.example.recrutement.candidate.dto.PythonHybridMatchRequest;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.entity.Competence;
import com.example.recrutement.offre.entity.OffreEmploi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIMatchingService {

    private final PythonApiService pythonApiService;

    // Cache pour les scores détaillés
    private final Map<String, AIDetailedScoresDTO> detailedScoresCache = new HashMap<>();

    /**
     * Calcule le score de matching avec approche hybride (recommandée)
     * Utilise l'API Python pour le scoring sémantique + métier
     */
    public double calculateMatchingScore(CandidateProfile candidate, OffreEmploi offre) {
        try {
            log.info("🎯 Calcul du score de matching hybride");
            log.info("   Candidat: {} {}", candidate.getPrenom(), candidate.getNom());
            log.info("   Offre: {}", offre.getTitre());
            
            // Construire les textes pour l'IA sémantique
            String candidateText = buildCandidateText(candidate);
            String jobText = buildJobText(offre);
            
            // Préparer la requête hybride
            PythonHybridMatchRequest hybridRequest = PythonHybridMatchRequest.builder()
                    .candidateText(candidateText)
                    .jobText(jobText)
                    .candidateSkills(candidate.getCompetencesAsStrings())
                    .requiredSkills(offre.getCompetencesRequises())
                    .candidateExperience(candidate.getExperienceAnnees() != null ? candidate.getExperienceAnnees() : 0)
                    .requiredExperience(offre.getExperienceRequise() != null ? offre.getExperienceRequise() : 0)
                    .candidateDegree(candidate.getNiveauEtude())
                    .requiredDegree(offre.getNiveauEtude())
                    .build();
            
            // Appeler l'API Python pour le scoring hybride
            PythonMatchResponse response = pythonApiService.calculateHybridMatchingScore(hybridRequest);
            
            if (response != null && response.getGlobalScore() != null) {
                log.info("✅ Score hybride calculé: {}% (sémantique: {}%, compétences: {}%, diplôme: {}%, expérience: {}%)",
                        response.getGlobalScore(),
                        response.getSemanticScore(),
                        response.getSkillsScore(),
                        response.getDegreeScore(),
                        response.getExperienceScore());
                
                if (response.getMatchedSkills() != null && !response.getMatchedSkills().isEmpty()) {
                    log.info("   ✅ Compétences matchées: {}", response.getMatchedSkills());
                }
                if (response.getMissingSkills() != null && !response.getMissingSkills().isEmpty()) {
                    log.info("   ❌ Compétences manquantes: {}", response.getMissingSkills());
                }
                
                return response.getGlobalScore();
            }
            
            // Fallback si l'API Python n'est pas disponible
            log.warn("⚠️ API Python non disponible, utilisation du fallback");
            return calculateFallbackScore(candidate, offre);
            
        } catch (Exception e) {
            log.error("❌ Erreur calcul score: {}", e.getMessage());
            return calculateFallbackScore(candidate, offre);
        }
    }

    /**
     * 🆕 Récupère les scores DÉTAILLÉS du matching (avec tous les sous-scores)
     * Cette méthode est appelée par le frontend pour afficher les détails
     */
    public AIDetailedScoresDTO getDetailedMatchingScores(CandidateProfile candidate, OffreEmploi offre) {
        String cacheKey = candidate.getId() + "_" + offre.getId();
        if (detailedScoresCache.containsKey(cacheKey)) {
            return detailedScoresCache.get(cacheKey);
        }

        PythonHybridMatchRequest request = PythonHybridMatchRequest.builder()
                .candidateText(buildCandidateText(candidate))
                .jobText(buildJobText(offre))
                .candidateSkills(candidate.getCompetencesAsStrings())
                .requiredSkills(offre.getCompetencesRequises())
                .candidateExperience(candidate.getExperienceAnnees())
                .requiredExperience(offre.getExperienceRequise())
                .candidateDegree(candidate.getNiveauEtude())
                .requiredDegree(offre.getNiveauEtude())
                .candidateTitre(candidate.getTitreProfessionnel())
                .jobTitre(offre.getTitre())
                .build();

        PythonMatchResponse response = pythonApiService.calculateHybridMatchingScore(request);
        AIDetailedScoresDTO scores = new AIDetailedScoresDTO();

        if (response != null && response.getGlobalScore() != null) {
            scores.setGlobalScore(response.getGlobalScore());
            scores.setSemanticScore(response.getSemanticScore());
            scores.setSkillsScore(response.getSkillsScore());
            scores.setDegreeScore(response.getDegreeScore());
            scores.setExperienceScore(response.getExperienceScore());
            scores.setTitleScore(response.getTitleScore());
            scores.setMatchedSkills(response.getMatchedSkills());
            scores.setMissingSkills(response.getMissingSkills());
            scores.setRecommendation(response.getRecommendation());
            scores.setConfidence(response.getConfidence());
        } else {
            // Fallback
            scores = calculateFallbackDetailedScores(candidate, offre);
        }

        detailedScoresCache.put(cacheKey, scores);
        return scores;
    }

    /**
     * Calcule les scores détaillés manuellement (fallback si API Python indisponible)
     */
    private AIDetailedScoresDTO calculateFallbackDetailedScores(CandidateProfile candidate, OffreEmploi offre) {
        log.info("📊 Calcul manuel des scores détaillés (fallback)");
        
        AIDetailedScoresDTO scores = new AIDetailedScoresDTO();
        
        // 1. Score compétences (0-100)
        double skillsScore = calculateSkillsScore(candidate, offre);
        scores.setSkillsScore(skillsScore);
        
        // 2. Score expérience (0-100)
        double expScore = calculateExperienceScore(candidate, offre);
        scores.setExperienceScore(expScore);
        
        // 3. Score diplôme (0-100)
        double degreeScore = calculateDegreeScore(candidate, offre);
        scores.setDegreeScore(degreeScore);
        
        // 4. Score titre (basé sur le titre professionnel)
        double titleScore = calculateTitleScore(candidate, offre);
        scores.setTitleScore(titleScore);
        
        // 5. Score sémantique (moyenne pondérée)
        double semanticScore = (skillsScore * 0.5) + (expScore * 0.25) + (degreeScore * 0.25);
        scores.setSemanticScore(semanticScore);
        
        // 6. Score global (pondération)
        double globalScore = (skillsScore * 0.40) + (expScore * 0.30) + (degreeScore * 0.20) + (titleScore * 0.10);
        scores.setGlobalScore(globalScore);
        
        // 7. Compétences matchées et manquantes
        List<String> matchedSkills = getMatchedSkills(candidate, offre);
        List<String> missingSkills = getMissingSkills(candidate, offre);
        scores.setMatchedSkills(matchedSkills);
        scores.setMissingSkills(missingSkills);
        
        // 8. Recommandation
        if (globalScore >= 80) {
            scores.setRecommendation("EXCELLENT - Candidat très bien adapté");
            scores.setConfidence("high");
        } else if (globalScore >= 65) {
            scores.setRecommendation("BON - Candidat recommandé");
            scores.setConfidence("high");
        } else if (globalScore >= 50) {
            scores.setRecommendation("MOYEN - À considérer avec formation");
            scores.setConfidence("medium");
        } else {
            scores.setRecommendation("FAIBLE - Non recommandé pour ce poste");
            scores.setConfidence("low");
        }
        
        log.info("📊 Scores fallback calculés: Global={}%, Skills={}%, Exp={}%, Degree={}%",
                Math.round(globalScore), Math.round(skillsScore), 
                Math.round(expScore), Math.round(degreeScore));
        
        return scores;
    }

    /**
     * Calcule le score des compétences (0-100)
     */
    private double calculateSkillsScore(CandidateProfile candidate, OffreEmploi offre) {
        List<String> candidateSkills = candidate.getCompetencesAsStrings();
        List<String> requiredSkills = offre.getCompetencesRequises();
        
        if (requiredSkills == null || requiredSkills.isEmpty()) return 100.0;
        if (candidateSkills == null || candidateSkills.isEmpty()) return 0.0;
        
        List<String> requiredLower = requiredSkills.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toList());
        
        List<String> candidateLower = candidateSkills.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toList());
        
        int matchingCount = 0;
        for (String required : requiredLower) {
            for (String candidateSkill : candidateLower) {
                if (candidateSkill.contains(required) || required.contains(candidateSkill)) {
                    matchingCount++;
                    break;
                }
            }
        }
        
        return (matchingCount * 100.0 / requiredLower.size());
    }

    /**
     * Calcule le score d'expérience (0-100)
     */
    private double calculateExperienceScore(CandidateProfile candidate, OffreEmploi offre) {
        Double candidateExp = candidate.getExperienceAnnees() != null ? candidate.getExperienceAnnees() : 0.0;
        Double requiredExp = offre.getExperienceRequise() != null ? offre.getExperienceRequise() : 0.0;
        
        if (requiredExp <= 0) return 100.0;
        if (candidateExp <= 0) return 0.0;
        if (candidateExp >= requiredExp) return 100.0;
        
        return (candidateExp / requiredExp) * 100;
    }

    /**
     * Calcule le score du diplôme (0-100)
     */
    private double calculateDegreeScore(CandidateProfile candidate, OffreEmploi offre) {
        String candidateDegree = candidate.getNiveauEtude() != null ? candidate.getNiveauEtude().toLowerCase() : "";
        String requiredDegree = offre.getNiveauEtude() != null ? offre.getNiveauEtude().toLowerCase() : "";
        
        if (requiredDegree.isEmpty()) return 100.0;
        if (candidateDegree.isEmpty()) return 30.0;
        
        // Niveaux d'étude
        Map<String, Integer> degreeLevels = new HashMap<>();
        degreeLevels.put("bac", 1);
        degreeLevels.put("bts", 2);
        degreeLevels.put("dut", 2);
        degreeLevels.put("licence", 3);
        degreeLevels.put("bachelor", 3);
        degreeLevels.put("master", 4);
        degreeLevels.put("ingenieur", 4);
        degreeLevels.put("doctorat", 5);
        degreeLevels.put("phd", 5);
        
        int candidateLevel = 0;
        int requiredLevel = 0;
        
        for (Map.Entry<String, Integer> entry : degreeLevels.entrySet()) {
            if (candidateDegree.contains(entry.getKey())) {
                candidateLevel = Math.max(candidateLevel, entry.getValue());
            }
            if (requiredDegree.contains(entry.getKey())) {
                requiredLevel = Math.max(requiredLevel, entry.getValue());
            }
        }
        
        if (requiredLevel == 0) return 100.0;
        if (candidateLevel == 0) return 30.0;
        if (candidateLevel >= requiredLevel) return 100.0;
        
        return (candidateLevel * 100.0 / requiredLevel);
    }

    /**
     * Calcule le score du titre (0-100)
     */
    private double calculateTitleScore(CandidateProfile candidate, OffreEmploi offre) {
        String candidateTitle = candidate.getTitreProfessionnel() != null ? candidate.getTitreProfessionnel().toLowerCase() : "";
        String jobTitle = offre.getTitre() != null ? offre.getTitre().toLowerCase() : "";
        
        if (jobTitle.isEmpty()) return 100.0;
        if (candidateTitle.isEmpty()) return 50.0;
        
        if (candidateTitle.equals(jobTitle)) return 100.0;
        
        // Mots communs
        Set<String> candidateWords = new HashSet<>(Arrays.asList(candidateTitle.split("\\s+")));
        Set<String> jobWords = new HashSet<>(Arrays.asList(jobTitle.split("\\s+")));
        
        candidateWords.retainAll(jobWords);
        
        if (candidateWords.isEmpty()) return 50.0;
        
        return Math.min(100.0, 60 + (candidateWords.size() * 10.0));
    }

    /**
     * Récupère les compétences matchées
     */
    private List<String> getMatchedSkills(CandidateProfile candidate, OffreEmploi offre) {
        List<String> candidateSkills = candidate.getCompetencesAsStrings();
        List<String> requiredSkills = offre.getCompetencesRequises();
        
        if (requiredSkills == null || requiredSkills.isEmpty()) return new ArrayList<>();
        if (candidateSkills == null || candidateSkills.isEmpty()) return new ArrayList<>();
        
        List<String> matched = new ArrayList<>();
        List<String> requiredLower = requiredSkills.stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> candidateLower = candidateSkills.stream().map(String::toLowerCase).collect(Collectors.toList());
        
        for (int i = 0; i < requiredSkills.size(); i++) {
            String required = requiredLower.get(i);
            for (String candidateSkill : candidateLower) {
                if (candidateSkill.contains(required) || required.contains(candidateSkill)) {
                    matched.add(requiredSkills.get(i));
                    break;
                }
            }
        }
        
        return matched;
    }

    /**
     * Récupère les compétences manquantes
     */
    private List<String> getMissingSkills(CandidateProfile candidate, OffreEmploi offre) {
        List<String> candidateSkills = candidate.getCompetencesAsStrings();
        List<String> requiredSkills = offre.getCompetencesRequises();
        
        if (requiredSkills == null || requiredSkills.isEmpty()) return new ArrayList<>();
        if (candidateSkills == null || candidateSkills.isEmpty()) return requiredSkills;
        
        List<String> missing = new ArrayList<>();
        List<String> requiredLower = requiredSkills.stream().map(String::toLowerCase).collect(Collectors.toList());
        List<String> candidateLower = candidateSkills.stream().map(String::toLowerCase).collect(Collectors.toList());
        
        for (int i = 0; i < requiredSkills.size(); i++) {
            String required = requiredLower.get(i);
            boolean found = false;
            for (String candidateSkill : candidateLower) {
                if (candidateSkill.contains(required) || required.contains(candidateSkill)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(requiredSkills.get(i));
            }
        }
        
        return missing;
    }



    /**
     * Construit le texte représentant le candidat
     */
    private String buildCandidateText(CandidateProfile candidate) {
        StringBuilder sb = new StringBuilder();
        
        if (candidate.getTitreProfessionnel() != null && !candidate.getTitreProfessionnel().isEmpty()) {
            sb.append(candidate.getTitreProfessionnel()).append(". ");
        }
        
        if (candidate.getCompetences() != null && !candidate.getCompetences().isEmpty()) {
            sb.append("Compétences: ");
            sb.append(String.join(", ", candidate.getCompetencesAsStrings()));
            sb.append(". ");
        }
        
        if (candidate.getExperienceAnnees() != null && candidate.getExperienceAnnees() > 0) {
            sb.append(candidate.getExperienceAnnees()).append(" ans d'expérience. ");
        }
        
        if (candidate.getNiveauEtude() != null && !candidate.getNiveauEtude().isEmpty()) {
            sb.append("Diplôme: ").append(candidate.getNiveauEtude()).append(". ");
        }
        
        if (candidate.getFormation() != null && !candidate.getFormation().isEmpty()) {
            sb.append("Formation: ").append(candidate.getFormation()).append(". ");
        }
        
        return sb.toString();
    }

    /**
     * Construit le texte représentant l'offre
     */
    private String buildJobText(OffreEmploi offre) {
        StringBuilder sb = new StringBuilder();
        
        if (offre.getTitre() != null && !offre.getTitre().isEmpty()) {
            sb.append(offre.getTitre()).append(". ");
        }
        
        if (offre.getDescription() != null && !offre.getDescription().isEmpty()) {
            sb.append(offre.getDescription()).append(". ");
        }
        
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
     * Score fallback si l'API Python n'est pas disponible
     */
    private double calculateFallbackScore(CandidateProfile candidate, OffreEmploi offre) {
        log.info("📊 Utilisation du calcul fallback (mode dégradé)");
        
        double skillsScore = calculateSkillsScore(candidate, offre);
        double expScore = calculateExperienceScore(candidate, offre);
        double degreeScore = calculateDegreeScore(candidate, offre);
        
        // Pondération: compétences 50%, expérience 30%, diplôme 20%
        double finalScore = (skillsScore * 0.5) + (expScore * 0.3) + (degreeScore * 0.2);
        
        log.info("📊 Score fallback final: {}%", Math.round(finalScore));
        return finalScore;
    }

    /**
     * Analyse détaillée du matching pour affichage RH
     */
    public Map<String, Object> getDetailedAnalysis(CandidateProfile candidate, OffreEmploi offre) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Récupérer les scores détaillés
        AIDetailedScoresDTO scores = getDetailedMatchingScores(candidate, offre);
        
        analysis.put("global_score", scores.getGlobalScore());
        analysis.put("skills_score", scores.getSkillsScore());
        analysis.put("experience_score", scores.getExperienceScore());
        analysis.put("degree_score", scores.getDegreeScore());
        analysis.put("semantic_score", scores.getSemanticScore());
        analysis.put("title_score", scores.getTitleScore());
        analysis.put("matched_skills", scores.getMatchedSkills());
        analysis.put("missing_skills", scores.getMissingSkills());
        analysis.put("recommendation", scores.getRecommendation());
        analysis.put("confidence", scores.getConfidence());
        
        // Compétences
        if (candidate.getCompetences() != null && !candidate.getCompetences().isEmpty()) {
            analysis.put("candidate_skills", candidate.getCompetencesAsStrings());
        }
        
        if (offre.getCompetencesRequises() != null && !offre.getCompetencesRequises().isEmpty()) {
            analysis.put("required_skills", offre.getCompetencesRequises());
        }
        
        // Expérience
        analysis.put("candidate_experience", candidate.getExperienceAnnees() != null ? candidate.getExperienceAnnees() : 0);
        analysis.put("required_experience", offre.getExperienceRequise() != null ? offre.getExperienceRequise() : 0);
        
        // Diplôme
        analysis.put("candidate_education", candidate.getNiveauEtude() != null ? candidate.getNiveauEtude() : "Non renseigné");
        analysis.put("required_education", offre.getNiveauEtude() != null ? offre.getNiveauEtude() : "Non spécifié");
        
        return analysis;
    }

    /**
     * Vide le cache des scores détaillés
     */
    public void clearCache() {
        detailedScoresCache.clear();
        log.info("🗑️ Cache des scores détaillés vidé");
    }
}