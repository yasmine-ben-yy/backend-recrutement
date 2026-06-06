package com.example.recrutement.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatDetailResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private String ville;
    private String pays;
    private String titreProfessionnel;
    private Integer experienceAnnees;
    private String niveauEtude;
    private String disponibilite;
    private String formation;

    private List<String> competences;  // Garder List<String>
    private String linkedinUrl;
    private String portfolioUrl;
    private String cvPrincipalPath;
    private String lettreMotivationPath;
    private String statut;
    private Integer nombreCandidatures;
    private LocalDate dateInscription;
    private List<CandidatureSimplifieeDTO> candidatures;
}