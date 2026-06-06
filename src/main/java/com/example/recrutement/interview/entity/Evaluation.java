package com.example.recrutement.interview.entity;

import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private CandidateProfile candidat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private OffreEmploi offre;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluateur_id", nullable = false)
    private User evaluateur;
    
    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_entretien")
    private InterviewTypeEntretien typeEntretien;
    
    @Column(name = "duree_minutes")
    private Integer dureeMinutes;
    
    @Column(name = "score_total")
    private Double scoreTotal;
    
    @Column(name = "points_forts", columnDefinition = "TEXT")
    private String pointsFort;
    
    @Column(name = "points_faibles", columnDefinition = "TEXT")
    private String pointsFaibles;
    
    @Column(name = "commentaires", columnDefinition = "TEXT")
    private String commentaires;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "decision_finale")
    private DecisionFinale decisionFinale;
    
    @Column(name = "recommandation", columnDefinition = "TEXT")
    private String recommandation;
    
    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvaluationCriteres> criteres = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum InterviewTypeEntretien {
        PHONE, VIDEO, PRESENTIEL, TECHNICAL, HR
    }
    
    public enum DecisionFinale {
        ACCEPTE, REFUSE, EN_ATTENTE, A_REVOIR
    }
    
    public void addCritere(EvaluationCriteres critere) {
        criteres.add(critere);
        critere.setEvaluation(this);
    }
    
    public Double calculerScoreTotal() {
        if (criteres == null || criteres.isEmpty()) return 0.0;
        double total = criteres.stream()
                .mapToDouble(ec -> ec.getNote() != null ? ec.getNote() : 0)
                .sum();
        return total / criteres.size();
    }
}