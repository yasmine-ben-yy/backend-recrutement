package com.example.recrutement.offre.entity;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "offres_emploi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreEmploi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "offre_competences", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "competence")
    private List<String> competencesRequises = new ArrayList<>();
    
    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Candidature> candidatures = new ArrayList<>();
    
    @Column(nullable = false)
    private String niveauEtude;
    
    @Column(nullable = false)
    private Integer experienceRequise;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_contrat_id")
    private TypeContrat typeContrat;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domaine_id")
    private Domaine domaine;
    
    @Column(nullable = false)
    private String localisation;
    
    private Double salaire;
    private String fourchetteSalaire;
    
    @Builder.Default
    private Boolean teletravailPossible = false;
    
    private LocalDateTime datePublication;
    private LocalDateTime dateCloture;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OffreStatut statut = OffreStatut.BROUILLON;
    
    @Builder.Default
    private Integer nombrePostes = 1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum OffreStatut {
        BROUILLON, PUBLIEE, ARCHIVEE, CLOTUREE
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (statut == OffreStatut.PUBLIEE && datePublication == null) {
            datePublication = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (statut == OffreStatut.PUBLIEE && datePublication == null) {
            datePublication = LocalDateTime.now();
        }
    }
    
    public boolean estExpiree() {
        if (dateCloture == null) return false;
        return LocalDateTime.now().isAfter(dateCloture);
    }
    
    public boolean estPubliee() {
        return statut == OffreStatut.PUBLIEE;
    }
    
    public boolean peutPostuler() {
        return estPubliee() && !estExpiree() && statut != OffreStatut.CLOTUREE;
    }
    
    public void addCandidature(Candidature candidature) {
        candidatures.add(candidature);
        candidature.setOffre(this);
    }
}