package com.example.recrutement.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_criteres")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCriteres {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "critere", nullable = false, length = 50)
    private EvaluationCritere critere;
    
    @Column(name = "note")
    private Integer note;
    
    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum EvaluationCritere {
        TECHNIQUE("Compétences techniques"),
        COMMUNICATION("Communication"),
        SOFT_SKILLS("Soft skills"),
        MOTIVATION("Motivation"),
        CULTURE("Culture entreprise"),
        PROBLEM_SOLVING("Résolution de problèmes");
        
        private final String label;
        
        EvaluationCritere(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
}