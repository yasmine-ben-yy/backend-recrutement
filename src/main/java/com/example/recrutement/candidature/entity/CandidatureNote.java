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
public class CandidatureNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Candidature candidature;

    @ManyToOne
    private User rh;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private LocalDateTime createdAt;

}