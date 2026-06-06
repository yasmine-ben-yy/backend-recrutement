package com.example.recrutement.candidate.entity;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Infos personnelles ---
    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String telephone;

    private LocalDate dateNaissance;
    private String ville;
    private String pays;
    private String email;  // ← AJOUTER CE CHAMP !


    // --- Infos professionnelles ---
    private String titreProfessionnel;
    private Integer experienceAnnees;
    private String niveauEtude;
    private String disponibilite;
 // Ajoutez ce champ dans CandidateProfile.java si vous l'utilisez
    @Column(columnDefinition = "TEXT")
    private String formation;

    // --- Compétences (maintenant en relation OneToMany) ---
    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Competence> competences = new ArrayList<>();
    
    // --- Documents ---
    private String linkedinUrl;
    private String portfolioUrl;
    private String cvPrincipalPath;

    @Column(columnDefinition = "TEXT")
    private String lettreMotivationPath;

    // --- Liaison avec le compte User ---
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // --- Relation avec les candidatures ---
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Candidature> candidatures = new ArrayList<>();
    
    // --- Méthodes utilitaires pour les compétences ---
    
    public void addCompetence(Competence competence) {
        competences.add(competence);
        competence.setCandidateProfile(this);
    }
    
    public void removeCompetence(Competence competence) {
        competences.remove(competence);
        competence.setCandidateProfile(null);
    }
    
    public void addCompetenceByName(String competenceName) {
        Competence competence = Competence.builder()
                .nom(competenceName)
                .candidateProfile(this)
                .build();
        competences.add(competence);
    }
    
    public void setCompetencesFromStrings(List<String> competenceNames) {
        // Nettoyer la liste existante
        competences.clear();
        
        // Ajouter les nouvelles compétences
        if (competenceNames != null) {
            for (String name : competenceNames) {
                addCompetenceByName(name.trim());
            }
        }
    }
    
    public List<String> getCompetencesAsStrings() {
        return competences.stream()
                .map(Competence::getNom)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // --- Méthodes utilitaires pour les candidatures ---
    
    public void addCandidature(Candidature candidature) {
        candidatures.add(candidature);
        candidature.setCandidate(this);
    }
    
    public void removeCandidature(Candidature candidature) {
        candidatures.remove(candidature);
        candidature.setCandidate(null);
    }
    
    public int getNombreCandidatures() {
        return candidatures != null ? candidatures.size() : 0;
    }
    
    public boolean aPostuleAOffre(Long offreId) {
        if (candidatures == null || candidatures.isEmpty()) {
            return false;
        }
        return candidatures.stream()
                .anyMatch(c -> c.getOffre() != null && 
                              c.getOffre().getId().equals(offreId));
    }
    
    @Override
    public String toString() {
        return "CandidateProfile{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", titreProfessionnel='" + titreProfessionnel + '\'' +
                ", experienceAnnees=" + experienceAnnees +
                '}';
    }

	
	
}