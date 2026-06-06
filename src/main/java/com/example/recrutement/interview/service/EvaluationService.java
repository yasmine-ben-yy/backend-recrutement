package com.example.recrutement.interview.service;

import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.entity.CandidatureStatusHistory;
import com.example.recrutement.candidature.entity.StatutCandidature;
import com.example.recrutement.candidature.repository.CandidatureHistoryRepository;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import com.example.recrutement.interview.dto.EvaluationCritereDTO;
import com.example.recrutement.interview.dto.EvaluationDTO;
import com.example.recrutement.interview.entity.Evaluation;
import com.example.recrutement.interview.entity.EvaluationCriteres;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.interview.repository.EvaluationCriteresRepository;
import com.example.recrutement.interview.repository.EvaluationRepository;
import com.example.recrutement.interview.repository.InterviewRepository;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.repository.OffreRepository;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationCriteresRepository evaluationCriteresRepository;
    private final InterviewRepository interviewRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final OffreRepository offreRepository;
    private final UserRepository userRepository;
    private final CandidatureRepository candidatureRepository;
    private final CandidatureHistoryRepository historyRepository;

    @Transactional
    public EvaluationDTO createEvaluation(Long interviewId, EvaluationDTO dto, Long evaluateurId) {
        log.info("=== CREATE EVALUATION ===");
        log.info("interviewId: {}", interviewId);
        log.info("evaluateurId: {}", evaluateurId);
        log.info("DTO candidatId: {}", dto.getCandidatId());
        log.info("DTO offreId: {}", dto.getOffreId());
        
        try {
            // 1. Vérifier que le DTO a les IDs nécessaires
            if (dto.getCandidatId() == null) {
                throw new RuntimeException("candidatId est requis dans le DTO");
            }
            if (dto.getOffreId() == null) {
                throw new RuntimeException("offreId est requis dans le DTO");
            }
            
            // 2. Récupérer l'interview
            Interview interview = interviewRepository.findById(interviewId)
                    .orElseThrow(() -> new RuntimeException("Entretien non trouvé avec ID: " + interviewId));
            log.info("Interview trouvée: {}", interview.getId());
            
            // 3. Récupérer le candidat
            CandidateProfile candidat = candidateProfileRepository.findById(dto.getCandidatId())
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec ID: " + dto.getCandidatId()));
            log.info("Candidat trouvé: ID={}, Nom={}", candidat.getId(), candidat.getNom());
            
            // 4. Récupérer l'offre
            OffreEmploi offre = offreRepository.findById(dto.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre non trouvée avec ID: " + dto.getOffreId()));
            log.info("Offre trouvée: ID={}, Titre={}", offre.getId(), offre.getTitre());
            
            // 5. Récupérer l'évaluateur
            User evaluateur = userRepository.findById(evaluateurId)
                    .orElseThrow(() -> new RuntimeException("Évaluateur non trouvé avec ID: " + evaluateurId));
            log.info("Évaluateur trouvé: ID={}", evaluateur.getId());
            
            // 6. Créer l'évaluation
            Evaluation.InterviewTypeEntretien typeEntretien = Evaluation.InterviewTypeEntretien.TECHNICAL;
            if (dto.getTypeEntretien() != null) {
                try {
                    typeEntretien = Evaluation.InterviewTypeEntretien.valueOf(dto.getTypeEntretien());
                } catch (IllegalArgumentException e) {
                    log.warn("Type entretien non reconnu: {}, utilisation TECHNICAL", dto.getTypeEntretien());
                }
            }
            
            Evaluation.DecisionFinale decisionFinale = Evaluation.DecisionFinale.EN_ATTENTE;
            if (dto.getDecisionFinale() != null) {
                try {
                    decisionFinale = Evaluation.DecisionFinale.valueOf(dto.getDecisionFinale());
                } catch (IllegalArgumentException e) {
                    log.warn("Décision finale non reconnue: {}, utilisation EN_ATTENTE", dto.getDecisionFinale());
                }
            }
            
            Evaluation evaluation = Evaluation.builder()
                    .interview(interview)
                    .candidat(candidat)
                    .offre(offre)
                    .evaluateur(evaluateur)
                    .dateEvaluation(LocalDateTime.now())
                    .typeEntretien(typeEntretien)
                    .dureeMinutes(dto.getDureeMinutes() != null ? dto.getDureeMinutes() : 60)
                    .pointsFort(dto.getPointsFort())
                    .pointsFaibles(dto.getPointsFaibles())
                    .commentaires(dto.getCommentaires())
                    .decisionFinale(decisionFinale)
                    .recommandation(dto.getRecommandation())
                    .build();
            
            log.info("Evaluation entity créée");
            
            // 7. Sauvegarder l'évaluation
            Evaluation saved = evaluationRepository.save(evaluation);
            log.info("Evaluation sauvegardée avec ID: {}", saved.getId());
            
            // 8. Ajouter les critères
            if (dto.getCriteres() != null && !dto.getCriteres().isEmpty()) {
                for (EvaluationCritereDTO critereDTO : dto.getCriteres()) {
                    EvaluationCriteres.EvaluationCritere critereEnum;
                    try {
                        critereEnum = EvaluationCriteres.EvaluationCritere.valueOf(critereDTO.getCritere().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Critère non reconnu: {}, utilisation TECHNIQUE", critereDTO.getCritere());
                        critereEnum = EvaluationCriteres.EvaluationCritere.TECHNIQUE;
                    }
                    
                    EvaluationCriteres critere = EvaluationCriteres.builder()
                            .evaluation(saved)
                            .critere(critereEnum)
                            .note(critereDTO.getNote() != null ? critereDTO.getNote() : 0)
                            .commentaire(critereDTO.getCommentaire())
                            .build();
                    evaluationCriteresRepository.save(critere);
                }
                log.info("Critères sauvegardés: {} critères", dto.getCriteres().size());
            }
            
            // 9. Recalculer le score total
            double totalScore = saved.getCriteres().stream()
                    .mapToDouble(EvaluationCriteres::getNote)
                    .average()
                    .orElse(0.0);
            saved.setScoreTotal(totalScore);
            evaluationRepository.save(saved);
            log.info("Score total calculé: {}", totalScore);
            
            // 10. Mettre à jour le statut de la candidature
            updateCandidatureStatusFromEvaluation(interview.getCandidature(), saved);
            
            return convertToDTO(saved);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'évaluation: ", e);
            throw new RuntimeException("Erreur création évaluation: " + e.getMessage(), e);
        }
    }
    
    private void updateCandidatureStatusFromEvaluation(Candidature candidature, Evaluation evaluation) {
        log.info("🔄 Mise à jour du statut candidature {} à partir de l'évaluation {}", 
                 candidature.getId(), evaluation.getId());
        
        if (candidature == null) {
            log.warn("⚠️ Candidature null, impossible de mettre à jour le statut");
            return;
        }
        
        StatutCandidature nouveauStatut = null;
        String raison = "";
        
        // Règle 1 : Score < 4 → élimination automatique
        if (evaluation.getScoreTotal() != null && evaluation.getScoreTotal() < 4.0) {
            nouveauStatut = StatutCandidature.ELIMINE;
            raison = String.format("Score total insuffisant : %.1f/10", evaluation.getScoreTotal());
            log.warn("⚠️ Élimination automatique - Score: {}/10", evaluation.getScoreTotal());
        }
        // Règle 2 : Score >= 8 → recommandation forte
        else if (evaluation.getScoreTotal() != null && evaluation.getScoreTotal() >= 8.0) {
            log.info("⭐ Recommandation forte - Score: {}/10", evaluation.getScoreTotal());
        }
        
        // Règle 3 : Décision finale
        if (nouveauStatut == null && evaluation.getDecisionFinale() != null) {
            switch (evaluation.getDecisionFinale()) {
                case ACCEPTE:
                    nouveauStatut = StatutCandidature.RETENUE;
                    raison = "Recommandé suite à l'entretien";
                    log.info("✅ Candidature recommandée → Statut RETENUE");
                    break;
                case REFUSE:
                    nouveauStatut = StatutCandidature.ELIMINE;
                    raison = "Rejeté suite à l'entretien";
                    log.info("❌ Candidature rejetée → Statut ELIMINE");
                    break;
                case A_REVOIR:
                    nouveauStatut = StatutCandidature.ENTRETIEN;
                    raison = "À réévaluer, second entretien recommandé";
                    log.info("🔄 Candidature à réévaluer → Statut ENTRETIEN");
                    break;
                default:
                    log.info("⏳ Décision en attente, statut inchangé");
                    break;
            }
        }
        
        // Appliquer le changement
        if (nouveauStatut != null && nouveauStatut != candidature.getStatut()) {
            StatutCandidature ancienStatut = candidature.getStatut();
            candidature.setStatut(nouveauStatut);
            
            // Créer l'historique
            CandidatureStatusHistory history = CandidatureStatusHistory.builder()
                    .candidature(candidature)
                    .ancienStatut(ancienStatut)
                    .nouveauStatut(nouveauStatut)
                    .changedBy(evaluation.getEvaluateur())
                    .changeDate(LocalDateTime.now())
                    .commentaire(String.format("[Auto - Évaluation] %s", raison))
                    .build();
            
            historyRepository.save(history);
            candidature.addHistoriqueStatut(history);
            candidatureRepository.save(candidature);
            
            log.info("✅ Statut candidature {} mis à jour : {} → {}", 
                     candidature.getId(), ancienStatut, nouveauStatut);
        }
    }
    
    public EvaluationDTO getEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));
        return convertToDTO(evaluation);
    }
    
    public List<EvaluationDTO> getEvaluationsByCandidat(Long candidatId) {
        return evaluationRepository.findByCandidatId(candidatId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<EvaluationDTO> getEvaluationsByOffre(UUID offreId) {
        return evaluationRepository.findByOffreId(offreId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<EvaluationDTO> getEvaluationsByInterview(Long interviewId) {
        return evaluationRepository.findByInterviewId(interviewId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private EvaluationDTO convertToDTO(Evaluation evaluation) {
        if (evaluation == null) return null;
        
        return EvaluationDTO.builder()
                .id(evaluation.getId())
                .interviewId(evaluation.getInterview() != null ? evaluation.getInterview().getId() : null)
                .candidatId(evaluation.getCandidat() != null ? evaluation.getCandidat().getId() : null)
                .candidatNom(evaluation.getCandidat() != null ? evaluation.getCandidat().getNom() : null)
                .candidatPrenom(evaluation.getCandidat() != null ? evaluation.getCandidat().getPrenom() : null)
                .offreTitre(evaluation.getOffre() != null ? evaluation.getOffre().getTitre() : null)
                .offreId(evaluation.getOffre() != null ? evaluation.getOffre().getId() : null)
                .evaluateurId(evaluation.getEvaluateur() != null ? evaluation.getEvaluateur().getId() : null)
                .evaluateurNom(evaluation.getEvaluateur() != null ? evaluation.getEvaluateur().getEmail() : null)
                .dateEvaluation(evaluation.getDateEvaluation())
                .typeEntretien(evaluation.getTypeEntretien() != null ? evaluation.getTypeEntretien().name() : null)
                .dureeMinutes(evaluation.getDureeMinutes())
                .scoreTotal(evaluation.getScoreTotal())
                .pointsFort(evaluation.getPointsFort())
                .pointsFaibles(evaluation.getPointsFaibles())
                .commentaires(evaluation.getCommentaires())
                .decisionFinale(evaluation.getDecisionFinale() != null ? evaluation.getDecisionFinale().name() : null)
                .recommandation(evaluation.getRecommandation())
                .criteres(evaluation.getCriteres().stream()
                        .map(c -> EvaluationCritereDTO.builder()
                                .critere(c.getCritere().name())
                                .label(c.getCritere().getLabel())
                                .note(c.getNote())
                                .commentaire(c.getCommentaire())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(evaluation.getCreatedAt())
                .build();
    }
}