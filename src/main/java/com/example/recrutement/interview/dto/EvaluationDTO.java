package com.example.recrutement.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {
    private Long id;
    private Long interviewId;
    private Long candidatId;
    private String candidatNom;
    private String candidatPrenom;
    private String offreTitre;
    private UUID offreId; 
    private Long evaluateurId;
    private String evaluateurNom;
    private LocalDateTime dateEvaluation;
    private String typeEntretien;
    private Integer dureeMinutes;
    private Double scoreTotal;
    
    private String pointsFort;
    private String pointsFaibles;
    private String commentaires;
    private String decisionFinale;
    private String recommandation;
    private List<EvaluationCritereDTO> criteres;
    private LocalDateTime createdAt;
}