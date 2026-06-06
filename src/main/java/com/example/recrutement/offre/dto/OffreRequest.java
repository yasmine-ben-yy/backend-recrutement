package com.example.recrutement.offre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OffreRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    @NotBlank(message = "La description est obligatoire")
    private String description;
    
    @NotEmpty(message = "Au moins une compétence est requise")
    private List<String> competencesRequises;
    
    private String niveauEtude;
    
    // ✅ AJOUTER CE CHAMP
    @NotNull(message = "L'expérience requise est obligatoire")
    @Range(min = 0, max = 30, message = "L'expérience doit être entre 0 et 30 ans")
    private Integer experienceRequise;
    
    @NotNull(message = "Le type de contrat est obligatoire")
    private UUID typeContratId;
    
    @NotNull(message = "Le domaine est obligatoire")
    private UUID domaineId;
    
    @NotBlank(message = "La localisation est obligatoire")
    private String localisation;
    
    private Double salaire;
    
    private String fourchetteSalaire;
    
    private Boolean teletravailPossible;
    
    private LocalDateTime dateCloture;
    
    @Range(min = 1, message = "Le nombre de postes doit être au moins 1")
    private Integer nombrePostes;
}