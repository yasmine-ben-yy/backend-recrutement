package com.example.recrutement.candidature.entity;

import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "candidatures",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"candidate_id", "offre_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private CandidateProfile candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id")
    @JsonBackReference 
    private OffreEmploi offre;

    @Enumerated(EnumType.STRING)
    private StatutCandidature statut;

    private LocalDateTime dateCandidature;

    // CV utilisé lors candidature
    private String cvSnapshotPath;

    private String lettreMotivationPath;

    // IA ready
    private Double matchingScore;

    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CandidatureNote> notes = new ArrayList<>();

    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CandidatureStatusHistory> historiqueStatuts = new ArrayList<>();
    
    @Column(name = "email_sent")
    private Boolean emailSent = false;
    
    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    
    private List<Interview> interviews = new ArrayList<>();
    
    @Column(name = "email_sent_date")
    private LocalDateTime emailSentDate;
    
    @Column(name = "email_error")
    private String emailError;
    
 
    
    @Column(name = "email_retry_count")
    private Integer emailRetryCount = 0;
    
    // Méthodes utilitaires pour les notes
    public void addNote(CandidatureNote note) {
        notes.add(note);
        note.setCandidature(this);
    }

    public void removeNote(CandidatureNote note) {
        notes.remove(note);
        note.setCandidature(null);
    }

    // Méthodes utilitaires pour l'historique
    public void addHistoriqueStatut(CandidatureStatusHistory historique) {
        historiqueStatuts.add(historique);
        historique.setCandidature(this);
    }

    // Méthode pour vérifier si le candidat peut postuler
    public boolean peutPostuler() {
        return offre != null && offre.peutPostuler();
    }

    // Méthode pour obtenir le dernier statut
    public CandidatureStatusHistory getDernierHistorique() {
        if (historiqueStatuts.isEmpty()) {
            return null;
        }
        return historiqueStatuts.get(historiqueStatuts.size() - 1);
    }
    
    public void addInterview(Interview interview) {
        interviews.add(interview);
        interview.setCandidature(this);
    }

    public void removeInterview(Interview interview) {
        interviews.remove(interview);
        interview.setCandidature(null);
    }
    
    public Interview getLastInterview() {
        if (interviews == null || interviews.isEmpty()) return null;
        return interviews.stream()
                .max((a, b) -> a.getDateEntretien().compareTo(b.getDateEntretien()))
                .orElse(null);
    }
    
    // ✅ NOUVELLE MÉTHODE : Vérifier si une transition de statut est autorisée
    public boolean canTransitionTo(StatutCandidature newStatut) {
        if (newStatut == null || this.statut == null) return false;
        if (this.statut == newStatut) return true;
        
        return switch (this.statut) {
            case EN_ATTENTE -> newStatut == StatutCandidature.A_CONTACTER || newStatut == StatutCandidature.ELIMINE;
            case A_CONTACTER -> newStatut == StatutCandidature.ENTRETIEN || newStatut == StatutCandidature.ELIMINE;
            case ENTRETIEN -> newStatut == StatutCandidature.RETENUE || newStatut == StatutCandidature.ELIMINE;
            case RETENUE -> newStatut == StatutCandidature.RECRUTE || newStatut == StatutCandidature.ELIMINE;
            case RECRUTE, ELIMINE -> false;
        };
    }
    
    // ✅ NOUVELLE MÉTHODE : Obtenir les statuts autorisés suivants
    public List<StatutCandidature> getAllowedNextStatuses() {
        return switch (this.statut) {
            case EN_ATTENTE -> List.of(StatutCandidature.A_CONTACTER, StatutCandidature.ELIMINE);
            case A_CONTACTER -> List.of(StatutCandidature.ENTRETIEN, StatutCandidature.ELIMINE);
            case ENTRETIEN -> List.of(StatutCandidature.RETENUE, StatutCandidature.ELIMINE);
            case RETENUE -> List.of(StatutCandidature.RECRUTE, StatutCandidature.ELIMINE);
            case RECRUTE, ELIMINE -> List.of();
        };
    }

	public LocalDateTime getUpdatedAt() {
		// TODO Auto-generated method stub
		return null;
	}
}