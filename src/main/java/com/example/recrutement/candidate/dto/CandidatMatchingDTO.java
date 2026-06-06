package com.example.recrutement.candidate.dto;

import com.example.recrutement.candidate.entity.CandidateProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatMatchingDTO {
    
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String titreProfessionnel;
    private Integer experienceAnnees;
    private String niveauEtude;
    private String ville;
    private String formation;

    private String pays;
    private List<String> competences;
    private Double matchingScore;
    private String recommendationLevel;
    private String recommendationIcone;
    private List<String> pointsForts;
    private List<String> competencesManquantes;
    private List<String> recommandations;
    private Integer tauxCouvertureCompetences;
    
    public CandidatMatchingDTO(CandidateProfile profile, Double score) {
        this.id = profile.getId();
        this.nom = profile.getNom();
        this.prenom = profile.getPrenom();
        this.email = profile.getUser() != null ? profile.getUser().getEmail() : null;
        this.telephone = profile.getTelephone();
        this.titreProfessionnel = profile.getTitreProfessionnel();
        this.experienceAnnees = profile.getExperienceAnnees();
        this.niveauEtude = profile.getNiveauEtude();
        this.ville = profile.getVille();
        this.pays = profile.getPays();
        this.competences = profile.getCompetencesAsStrings();
        this.matchingScore = score;
        
        if (score >= 85) {
            this.recommendationLevel = "EXCEPTIONNEL - Priorité absolue";
            this.recommendationIcone = "🏆";
        } else if (score >= 75) {
            this.recommendationLevel = "TRÈS FORT - À contacter immédiatement";
            this.recommendationIcone = "⭐";
        } else if (score >= 65) {
            this.recommendationLevel = "FORT - Entretien recommandé";
            this.recommendationIcone = "👍";
        } else if (score >= 55) {
            this.recommendationLevel = "BON - À considérer";
            this.recommendationIcone = "📌";
        } else if (score >= 45) {
            this.recommendationLevel = "MOYEN - Peu pertinent";
            this.recommendationIcone = "🤔";
        } else if (score >= 35) {
            this.recommendationLevel = "FAIBLE - À écarter";
            this.recommendationIcone = "⚠️";
        } else {
            this.recommendationLevel = "TRÈS FAIBLE - Ne correspond pas";
            this.recommendationIcone = "❌";
        }
    }
}