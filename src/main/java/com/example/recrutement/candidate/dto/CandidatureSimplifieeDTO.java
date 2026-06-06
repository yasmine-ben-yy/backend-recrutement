package com.example.recrutement.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureSimplifieeDTO {
    private Long id;
    private String offreTitre;
    private String offreTypeContrat;
    private String offreLocalisation;
    private LocalDateTime dateCandidature;
    private String statut;
    private Double matchingScore;
}