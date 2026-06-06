// com/example/recrutement/candidate/dto/CandidatListResponseDTO.java
package com.example.recrutement.candidate.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatListResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String ville;
    private String pays;
    private String formation;

    private String titreProfessionnel;
    private Integer experienceAnnees;
    private String niveauEtude;
    private String disponibilite;
    private List<String> competences;
    private String cvPrincipalPath;
    private String statut;
    private Integer nombreCandidatures;
}