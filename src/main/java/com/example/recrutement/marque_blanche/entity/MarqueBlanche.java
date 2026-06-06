// com.example.recrutement.marque_blanche.entity.MarqueBlanche.java
package com.example.recrutement.marque_blanche.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "marque_blanche")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Configuration de la marque blanche pour l'application")
public class MarqueBlanche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identifiant unique de la configuration", example = "1")
    private Long id;

    @Column(name = "nom_application", length = 100, nullable = false)
    @Schema(description = "Nom de l'application affiché sur toutes les pages", example = "Votre entreprise", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nomApplication;

    @Column(name = "logo_entreprise", length = 255)
    @Schema(description = "URL ou chemin vers le logo de l'entreprise", example = "/logo-defaut.png")
    private String logoEntreprise;

    @Column(name = "couleur_primaire", length = 20)
    @Schema(description = "Couleur principale pour le texte et éléments importants (Tailwind / hex)", example = "text-purple-600")
    private String couleurPrimaire;

    @Column(name = "couleur_secondaire", length = 20)
    @Schema(description = "Couleur secondaire (arrière-plan, cartes, badges)", example = "bg-purple-100")
    private String couleurSecondaire;

    @Column(name = "couleur_accent", length = 20)
    @Schema(description = "Couleur d’accentuation (boutons, hover, highlights)", example = "text-orange-500")
    private String couleurAccent;

    @Column(name = "couleur_fond", length = 20)
    @Schema(description = "Couleur de fond générale de l'application", example = "bg-white")
    private String couleurFond;

    @Column(name = "texte_mentions_legales", columnDefinition = "TEXT")
    @Schema(description = "Texte des mentions légales affiché dans le footer ou page dédiée", example = "Mentions légales à compléter")
    private String texteMentionsLegales;

    @Column(name = "texte_politique_confidentialite", columnDefinition = "TEXT")
    @Schema(description = "Texte de la politique de confidentialité / RGPD", example = "Politique de confidentialité à compléter")
    private String textePolitiqueConfidentialite;

    @Column(name = "email_contact", length = 100)
    @Schema(description = "Email de contact affiché dans l'application", example = "contact@entreprise.com")
    private String emailContact;

    @Column(name = "telephone_contact", length = 20)
    @Schema(description = "Numéro de contact de l'entreprise", example = "+21600000000")
    private String telephoneContact;

    @Column(name = "adresse_entreprise", length = 255)
    @Schema(description = "Adresse physique de l'entreprise", example = "Rue de l’entreprise, Ville")
    private String adresseEntreprise;

    @Column(name = "site_web", length = 255)
    @Schema(description = "URL du site web de l'entreprise", example = "https://entreprise.com")
    private String siteWeb;

    @Column(name = "afficher_champ_telephone")
    @Schema(description = "Affiche le champ téléphone dans le formulaire candidat", example = "true")
    private Boolean afficherChampTelephone = true;

    @Column(name = "afficher_champ_linkedin")
    @Schema(description = "Affiche le champ LinkedIn dans le formulaire candidat", example = "true")
    private Boolean afficherChampLinkedin = true;

    @Column(name = "afficher_champ_portfolio")
    @Schema(description = "Affiche le champ portfolio / site personnel", example = "false")
    private Boolean afficherChampPortfolio = false;

    @Column(name = "afficher_champ_courriel")
    @Schema(description = "Affiche le champ email du candidat", example = "true")
    private Boolean afficherChampCourriel = true;

    @Column(name = "statut_initial_candidature", length = 20)
    @Schema(description = "Statut par défaut des nouvelles candidatures", example = "NOUVELLE")
    private String statutInitialCandidature = "NOUVELLE";

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    @Schema(description = "Date de création du paramètre", example = "2024-01-15T10:30:00")
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification")
    @Schema(description = "Date de dernière modification", example = "2024-01-15T14:45:00")
    private LocalDateTime dateModification;
}