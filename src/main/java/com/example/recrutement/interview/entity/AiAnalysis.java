// com.example.recrutement.interview.entity/AiAnalysis.java
package com.example.recrutement.interview.entity;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.offre.entity.OffreEmploi;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private OffreEmploi offre;
    
    @Column(columnDefinition = "TEXT")
    private String matchedSkills;
    
    @Column(columnDefinition = "TEXT")
    private String missingSkills;
    
    private Integer semanticScore;
    
    @Column(columnDefinition = "TEXT")
    private String recommendation;
    
    @Column(columnDefinition = "TEXT")
    private String strengths;
    
    @Column(columnDefinition = "TEXT")
    private String weaknesses;
    
    @Column(columnDefinition = "TEXT")
    private String recommendationsList;
    
    private Integer confidenceScore;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}