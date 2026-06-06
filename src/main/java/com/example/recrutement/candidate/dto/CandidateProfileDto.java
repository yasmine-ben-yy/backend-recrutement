// com.example.recrutement.candidate.dto.CandidateProfileDto.java
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
public class CandidateProfileDto {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private LocalDate dateNaissance;
    private String ville;
    private String pays;
    private String titreProfessionnel;
    private Integer experienceAnnees;
    private String formation;

    private String niveauEtude;
    private String disponibilite;
    private List<String> competences;
    private String linkedinUrl;
    private String portfolioUrl;
    private String cvPrincipalPath;
    private String lettreMotivationPath;
}