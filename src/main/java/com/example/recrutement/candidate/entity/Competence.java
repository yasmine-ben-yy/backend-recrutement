package com.example.recrutement.candidate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "competences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Competence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nom;
    private String niveau;
    
    @ManyToOne
    @JoinColumn(name = "candidate_profile_id")
    @JsonIgnore
    private CandidateProfile candidateProfile;
}