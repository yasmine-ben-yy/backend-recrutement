// com.example.recrutement.interview.entity/AiSummary.java
package com.example.recrutement.interview.entity;

import com.example.recrutement.candidature.entity.Candidature;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "candidature"})

public class AiSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;
    
    @Column(name = "main_skills", columnDefinition = "TEXT")
    private String mainSkills;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(name = "compatibility_score")
    private Integer compatibilityScore;
    
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;
    
    @Column(name = "candidate_level")
    private String candidateLevel;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}