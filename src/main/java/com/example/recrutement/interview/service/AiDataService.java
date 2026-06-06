// com.example.recrutement.interview.service/AiDataService.java
package com.example.recrutement.interview.service;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import com.example.recrutement.interview.entity.AiAnalysis;
import com.example.recrutement.interview.entity.AiSummary;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.interview.entity.InterviewQuestion;
import com.example.recrutement.interview.repository.AiAnalysisRepository;
import com.example.recrutement.interview.repository.AiSummaryRepository;
import com.example.recrutement.interview.repository.InterviewQuestionRepository;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDataService {

    private final InterviewQuestionRepository interviewQuestionRepository;
    private final AiSummaryRepository aiSummaryRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final CandidatureRepository candidatureRepository;
    private final OffreRepository offreRepository;

    @Transactional
    public List<InterviewQuestion> saveQuestions(Long interviewId, List<String> questions, String candidateLevel) {
        log.info("📝 Sauvegarde de {} questions IA pour l'entretien {}", questions.size(), interviewId);
        
        Interview interview = Interview.builder().id(interviewId).build();
        
        List<InterviewQuestion> savedQuestions = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            InterviewQuestion question = InterviewQuestion.builder()
                    .interview(interview)
                    .question(questions.get(i))
                    .typeQuestion(InterviewQuestion.QuestionType.SITUATION)
                    .orderIndex(i)
                    .build();
            savedQuestions.add(interviewQuestionRepository.save(question));
        }
        
        log.info("✅ {} questions sauvegardées", savedQuestions.size());
        return savedQuestions;
    }
 // Ajoutez ces méthodes dans AiDataService.java

    public AiAnalysis getAnalysisByCandidature(Long candidatureId) {
        log.info("📋 Recherche de l'analyse pour candidature: {}", candidatureId);
        
        try {
            // Vérifier que la candidature existe
            Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
            if (candidatureOpt.isEmpty()) {
                log.warn("⚠️ Candidature non trouvée: {}", candidatureId);
                return null;
            }
            
            Optional<AiAnalysis> analysisOpt = aiAnalysisRepository.findByCandidatureId(candidatureId);
            
            if (analysisOpt.isPresent()) {
                AiAnalysis analysis = analysisOpt.get();
                log.info("✅ Analyse trouvée: id={}, score={}", analysis.getId(), analysis.getSemanticScore());
                return analysis;
            } else {
                log.info("ℹ️ Aucune analyse trouvée pour candidature: {}", candidatureId);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche de l'analyse: {}", e.getMessage());
            return null;
        }
    }

    public AiAnalysis getAnalysisByCandidatureAndOffre(Long candidatureId, UUID offreId) {
        log.info("📋 Recherche de l'analyse pour candidature {} et offre {}", candidatureId, offreId);
        
        Optional<AiAnalysis> analysisOpt = aiAnalysisRepository.findByCandidatureIdAndOffreId(candidatureId, offreId);
        
        if (analysisOpt.isPresent()) {
            AiAnalysis analysis = analysisOpt.get();
            log.info("✅ Analyse trouvée: id={}", analysis.getId());
            return analysis;
        } else {
            log.warn("⚠️ Aucune analyse trouvée");
            return null;
        }
    }

    @Transactional
    public AiSummary saveSummary(Long candidatureId, String summary, List<String> mainSkills, 
                                  Integer experienceYears, Integer compatibilityScore, 
                                  List<String> recommendations, String candidateLevel) {
        log.info("📝 Sauvegarde du résumé IA pour candidature {}", candidatureId);
        
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec ID: " + candidatureId));
        
        AiSummary aiSummary = AiSummary.builder()
                .candidature(candidature)
                .summary(summary)
                .mainSkills(mainSkills != null ? String.join(",", mainSkills) : null)
                .experienceYears(experienceYears != null ? experienceYears : 0)
                .compatibilityScore(compatibilityScore != null ? compatibilityScore : 0)
                .recommendations(recommendations != null ? String.join("|", recommendations) : null)
                .candidateLevel(candidateLevel)
                .build();
        
        return aiSummaryRepository.save(aiSummary);
    }
    public AiSummary getSummaryByCandidature(Long candidatureId) {
        log.info("📋 Recherche du résumé pour candidature: {}", candidatureId);
        
        try {
            // Vérifier que la candidature existe
            Optional<Candidature> candidatureOpt = candidatureRepository.findById(candidatureId);
            if (candidatureOpt.isEmpty()) {
                log.warn("⚠️ Candidature non trouvée: {}", candidatureId);
                return null;
            }
            
            Optional<AiSummary> summaryOpt = aiSummaryRepository.findByCandidatureId(candidatureId);
            
            if (summaryOpt.isPresent()) {
                AiSummary summary = summaryOpt.get();
                log.info("✅ Résumé trouvé: id={}, score={}", summary.getId(), summary.getCompatibilityScore());
                return summary;
            } else {
                log.info("ℹ️ Aucun résumé trouvé pour candidature: {}", candidatureId);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de la recherche du résumé: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    public AiAnalysis saveAnalysis(Long candidatureId, UUID offreId, List<String> matchedSkills,
                                    List<String> missingSkills, Integer semanticScore, 
                                    String recommendation, List<String> strengths,
                                    List<String> weaknesses, List<String> recommendationsList,
                                    Integer confidenceScore) {
        log.info("📝 Sauvegarde de l'analyse IA pour candidature {} / offre {}", candidatureId, offreId);
        
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec ID: " + candidatureId));
        
        // ✅ offreId est UUID, pas besoin de conversion
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec ID: " + offreId));
        
        AiAnalysis analysis = AiAnalysis.builder()
                .candidature(candidature)
                .offre(offre)
                .matchedSkills(matchedSkills != null ? String.join(",", matchedSkills) : null)
                .missingSkills(missingSkills != null ? String.join(",", missingSkills) : null)
                .semanticScore(semanticScore != null ? semanticScore : 0)
                .recommendation(recommendation)
                .strengths(strengths != null ? String.join("|", strengths) : null)
                .weaknesses(weaknesses != null ? String.join("|", weaknesses) : null)
                .recommendationsList(recommendationsList != null ? String.join("|", recommendationsList) : null)
                .confidenceScore(confidenceScore != null ? confidenceScore : 0)
                .build();
        
        return aiAnalysisRepository.save(analysis);
    }

    public List<InterviewQuestion> getQuestionsByInterview(Long interviewId) {
        return interviewQuestionRepository.findByInterviewId(interviewId);
    }


}