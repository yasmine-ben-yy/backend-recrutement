package com.example.recrutement.candidature.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.recrutement.candidature.entity.StatutCandidature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureResponseDTO {
    private Long id;
    private LocalDateTime dateCandidature;
    private StatutCandidature statut;
    private Double matchingScore;
    private String cvSnapshotPath;
    private Long candidatId;      // ID direct du candidat
    private String offreId;
    private String lettreMotivationPath;
    private CandidatSimplifieDTO candidate;
    private OffreSimplifieeDTO offre;
    private List<HistoriqueStatutDTO> historiqueStatuts;
    private List<HistoriqueNoteDTO> historiqueNotes;
}