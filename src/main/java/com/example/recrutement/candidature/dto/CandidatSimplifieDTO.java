package com.example.recrutement.candidature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatSimplifieDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String titreProfessionnel;
    private Integer experienceAnnees;
    private String niveauEtude;
    private String ville;
    private String dateNaissance;
    private String pays;
    private String cvPrincipalPath;
}