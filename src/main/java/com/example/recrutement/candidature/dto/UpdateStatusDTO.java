package com.example.recrutement.candidature.dto;

import com.example.recrutement.candidature.entity.StatutCandidature;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusDTO {
    @NotNull(message = "Le statut est requis")
    private StatutCandidature statut;
    private String commentaire;
}