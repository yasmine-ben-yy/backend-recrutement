// src/main/java/com/example/recrutement/candidature/dto/OffreSimplifieeDTO.java
package com.example.recrutement.candidature.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OffreSimplifieeDTO {
    private UUID id;
    private String titre;
    private String description;
    private String localisation;
    private String typeContrat;
    private String domaine;
    private String niveauEtude;
    private String fourchetteSalaire;          // Optionnel
    private Boolean teletravailPossible;       // Optionnel
    private LocalDateTime dateCloture;          // Optionnel
    private List<String> competencesRequises;  // Optionnel

    private LocalDateTime datePublication;
    private String statut;
    private Integer nombrePostesRestants;
    private Boolean peutPostuler;
}