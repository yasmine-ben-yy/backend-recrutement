package com.example.recrutement.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class InterviewQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    @JsonIgnore
    private Interview interview;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;
    
    @Enumerated(EnumType.STRING)
    private QuestionType typeQuestion;
    
    private String competence;
    private Integer score;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    private Integer orderIndex;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum QuestionType {
        TECHNIQUE, SOFT_SKILL, COMPETENCE, MOTIVATION, SITUATION
    }
}