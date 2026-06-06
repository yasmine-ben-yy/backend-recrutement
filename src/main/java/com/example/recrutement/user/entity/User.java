// com.example.recrutement.user.entity.User.java
package com.example.recrutement.user.entity;

import com.example.recrutement.rh.entity.RhProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.recrutement.candidate.entity.CandidateProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;
    
    private String provider; // "GOOGLE" ou "LOCAL"
    private String providerId; // ID Google
    private String name; // Nom complet
    private String pictureUrl; // Photo de profil

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relation avec RH
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private RhProfile rhProfile;

    // Relation avec CandidateProfile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private CandidateProfile candidateProfile;
    // Token de vérification de compte
    private String verificationToken;
    private LocalDateTime verificationTokenExpiryDate;
    
    // Token de réinitialisation de mot de passe
    private String resetPasswordToken;
    private LocalDateTime resetTokenExpiryDate;

    // ✅ Constructeur utile pour la création rapide
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }
 // Généré automatiquement par Lombok
    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isEnabled() {
        return enabled;
    }
}