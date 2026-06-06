// com.example.recrutement.interview.dto.InterviewResponse.java
package com.example.recrutement.interview.dto;

import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.interview.entity.InterviewCommentaire;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {

    private Long id;
    private Long candidatureId;
    private Long candidatId;
    private String candidatNom;
    private String candidatPrenom;
    private String candidatPoste;
    private UUID offreId;
    private String offreTitre;
    private Interview.InterviewType type;
    private Interview.InterviewStatus statut;
    private LocalDateTime date;
    private Integer duree;
    private String lieu;
    private String meetingLink;
    private String notes;
    private List<CommentaireDto> commentaires;
    private Long createdById;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentaireDto {
        private Long id;
        private String contenu;
        private String createdBy;
        private LocalDateTime createdAt;
    }

    public static InterviewResponse fromEntity(Interview interview) {
        if (interview == null) return null;

        // ✅ Correction: utiliser new ArrayList<>() au lieu de List.of()
        List<CommentaireDto> commentairesDto = new ArrayList<>();
        
        if (interview.getCommentaires() != null && !interview.getCommentaires().isEmpty()) {
            commentairesDto = interview.getCommentaires().stream()
                .map(c -> new CommentaireDto(
                    c.getId(),
                    c.getContenu(),
                    c.getCreatedBy() != null ? c.getCreatedBy().getEmail() : "Système",
                    c.getCreatedAt()
                ))
                .collect(Collectors.toList());
        }

        // ✅ Récupération sécurisée des données
        Long candidatureId = interview.getCandidature() != null ? interview.getCandidature().getId() : null;
        
        Long candidatId = null;
        String candidatNom = "";
        String candidatPrenom = "";
        String candidatPoste = "";
        
        if (interview.getCandidature() != null && interview.getCandidature().getCandidate() != null) {
            candidatId = interview.getCandidature().getCandidate().getId();
            candidatNom = interview.getCandidature().getCandidate().getNom() != null ? 
                          interview.getCandidature().getCandidate().getNom() : "";
            candidatPrenom = interview.getCandidature().getCandidate().getPrenom() != null ? 
                            interview.getCandidature().getCandidate().getPrenom() : "";
            candidatPoste = interview.getCandidature().getCandidate().getTitreProfessionnel() != null ? 
                           interview.getCandidature().getCandidate().getTitreProfessionnel() : "";
        }
        
        UUID offreId = null;
        String offreTitre = "";
        
        if (interview.getCandidature() != null && interview.getCandidature().getOffre() != null) {
            offreId = interview.getCandidature().getOffre().getId();
            offreTitre = interview.getCandidature().getOffre().getTitre() != null ? 
                        interview.getCandidature().getOffre().getTitre() : "";
        }
        
        Long createdById = interview.getCreatedBy() != null ? interview.getCreatedBy().getId() : null;
        String createdByEmail = interview.getCreatedBy() != null ? interview.getCreatedBy().getEmail() : null;

        return new InterviewResponse(
            interview.getId(),
            candidatureId,
            candidatId,
            candidatNom,
            candidatPrenom,
            candidatPoste,
            offreId,
            offreTitre,
            interview.getType(),
            interview.getStatut(),
            interview.getDateEntretien(),
            interview.getDureeMinutes(),
            interview.getLieu(),
            interview.getMeetingLink(),
            interview.getNotes(),
            commentairesDto,
            createdById,
            createdByEmail,
            interview.getCreatedAt(),
            interview.getUpdatedAt()
        );
    }
}