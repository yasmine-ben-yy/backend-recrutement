package com.example.recrutement.candidature.entity;

import com.example.recrutement.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Candidature candidature;

    @Enumerated(EnumType.STRING)
    private StatutCandidature ancienStatut;

    @Enumerated(EnumType.STRING)
    private StatutCandidature nouveauStatut;

    @ManyToOne
    private User changedBy;

    private LocalDateTime changeDate;
    private String commentaire;

}