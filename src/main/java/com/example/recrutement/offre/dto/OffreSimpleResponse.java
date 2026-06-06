package com.example.recrutement.offre.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OffreSimpleResponse {
    private UUID id;
    private String titre;
    private String description;
    private List<String> competencesRequises;
    private String typeContrat;
    private String domaine;
    private String localisation;
    private String fourchetteSalaire;
    private Boolean teletravailPossible;
    private LocalDateTime datePublication;
    private LocalDateTime dateCloture;
    private Boolean peutPostuler;
     private Integer experienceRequise;
}