// com.example.recrutement.interview.entity.Interview.java
package com.example.recrutement.interview.entity;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.user.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_id", nullable = false)
    
    private Candidature candidature;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InterviewType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private InterviewStatus statut;
    
    @Column(name = "date_entretien", nullable = false)
    private LocalDateTime dateEntretien;
    
    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes = 60;
    
    @Column(name = "lieu")
    private String lieu;
    
    @Column(name = "meeting_link")
    private String meetingLink;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference  // Côté enfant

    private List<InterviewCommentaire> commentaires = new ArrayList<>();
    
    public enum InterviewType {
        RH, TECHNIQUE, FINAL, PHONE, VIDEO, PRESENTIEL
    }
    
    public enum InterviewStatus {
        PLANIFIE, CONFIRME, EN_COURS, TERMINE, ANNULE, REPORTE
    }
    
    // ✅ Méthode pour vérifier si l'entretien est modifiable
    public boolean estModifiable() {
        return statut == InterviewStatus.PLANIFIE || 
               statut == InterviewStatus.CONFIRME ||
               statut == InterviewStatus.REPORTE;  // Ajout de REPORTE comme modifiable
    }
    
    // ✅ Méthodes utilitaires
    public Long getCandidatId() {
        return candidature != null && candidature.getCandidate() != null ? 
               candidature.getCandidate().getId() : null;
    }
    
    public String getCandidatNom() {
        return candidature != null && candidature.getCandidate() != null ? 
               candidature.getCandidate().getNom() : "";
    }
    
    public String getCandidatPrenom() {
        return candidature != null && candidature.getCandidate() != null ? 
               candidature.getCandidate().getPrenom() : "";
    }
    
    public String getOffreTitre() {
        return candidature != null && candidature.getOffre() != null ? 
               candidature.getOffre().getTitre() : "";
    }
    
    public boolean estAVenir() {
        return dateEntretien.isAfter(LocalDateTime.now());
    }
    
    public boolean estPasse() {
        return dateEntretien.isBefore(LocalDateTime.now());
    }
    
    public void ajouterCommentaire(InterviewCommentaire commentaire) {
        if (commentaires == null) {
            commentaires = new ArrayList<>();
        }
        commentaires.add(commentaire);
        commentaire.setInterview(this);
    }
}