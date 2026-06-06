package com.example.recrutement.candidate.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfileResponseDto {

    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private LocalDate dateNaissance;
    private String ville;
    private String formation;

    private String pays;
    private String titreProfessionnel;
    private Integer experienceAnnees;
    private String niveauEtude;
    private String disponibilite;
    private List<String> competences;
    private String linkedinUrl;
    private String portfolioUrl;
    private String cvPrincipalPath;
    private String lettreMotivationPath;

    private UserResponseDto user; // relation vers le DTO de l'utilisateur
}