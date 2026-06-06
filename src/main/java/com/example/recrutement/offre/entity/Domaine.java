// src/main/java/com/example/recrutement/offre/entity/Domaine.java
package com.example.recrutement.offre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "domaines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Domaine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String nom;
    
    private String description;
    
    private String couleur; // Optionnel: code couleur hexadécimal
    
    @Column(nullable = false)
    private Boolean actif = true;
    
    @Column(nullable = false)
    private Integer ordre = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}