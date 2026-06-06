package com.example.recrutement.candidature.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatutDTO {
    private Long id;
    private LocalDateTime date;
    private String auteur;
    private String ancienStatut;
    private String nouveauStatut;
    private String commentaire;
}