package com.example.recrutement.offre.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OffreResponse {
    private UUID id;
    private String titre;
    private String description;
    private List<String> competencesRequises;
    private String niveauEtude;
    private Integer experienceRequise;
    
    // Type de contrat
    private String typeContrat;
    private UUID typeContratId;  
    
    // Domaine
    private String domaine;
    private UUID domaineId;  
    
    private String localisation;
    private Double salaire;
    private String fourchetteSalaire;
    private Boolean teletravailPossible;
    private LocalDateTime datePublication;
    private LocalDateTime dateCloture;
    private String statut;
    private Integer nombrePostes;
    private Boolean peutPostuler;
    private Boolean estExpiree;
    private LocalDateTime createdAt;
    private Long createdBy;
}