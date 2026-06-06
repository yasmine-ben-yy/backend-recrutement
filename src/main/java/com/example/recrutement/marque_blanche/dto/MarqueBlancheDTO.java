// com.example.recrutement.marque_blanche.dto.MarqueBlancheDTO.java
package com.example.recrutement.marque_blanche.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO pour la configuration de marque blanche")
public class MarqueBlancheDTO {
    
    @Schema(description = "Identifiant unique de la configuration", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "Nom de l'application affiché sur toutes les pages", example = "Votre entreprise", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nomApplication;
    
    @Schema(description = "URL ou chemin vers le logo de l'entreprise", example = "/logo-defaut.png")
    private String logoEntreprise;
    
    @Schema(description = "Couleur principale pour le texte et éléments importants (Tailwind / hex)", example = "text-purple-600")
    private String couleurPrimaire;
    
    @Schema(description = "Couleur secondaire (arrière-plan, cartes, badges)", example = "bg-purple-100")
    private String couleurSecondaire;
    
    @Schema(description = "Couleur d’accentuation (boutons, hover, highlights)", example = "text-orange-500")
    private String couleurAccent;
    
    @Schema(description = "Couleur de fond générale de l'application", example = "bg-white")
    private String couleurFond;
    
    @Schema(description = "Texte des mentions légales affiché dans le footer ou page dédiée", example = "Mentions légales à compléter")
    private String texteMentionsLegales;
    
    @Schema(description = "Texte de la politique de confidentialité / RGPD", example = "Politique de confidentialité à compléter")
    private String textePolitiqueConfidentialite;
    
    @Schema(description = "Email de contact affiché dans l'application", example = "contact@entreprise.com")
    private String emailContact;
    
    @Schema(description = "Numéro de contact de l'entreprise", example = "+21600000000")
    private String telephoneContact;
    
    @Schema(description = "Adresse physique de l'entreprise", example = "Rue de l’entreprise, Ville")
    private String adresseEntreprise;
    
    @Schema(description = "URL du site web de l'entreprise", example = "https://entreprise.com")
    private String siteWeb;
    
    @Schema(description = "Affiche le champ téléphone dans le formulaire candidat", example = "true")
    private Boolean afficherChampTelephone;
    
    @Schema(description = "Affiche le champ LinkedIn dans le formulaire candidat", example = "true")
    private Boolean afficherChampLinkedin;
    
    @Schema(description = "Affiche le champ portfolio / site personnel", example = "false")
    private Boolean afficherChampPortfolio;
    
    @Schema(description = "Affiche le champ email du candidat", example = "true")
    private Boolean afficherChampCourriel;
    
    @Schema(description = "Statut par défaut des nouvelles candidatures", example = "NOUVELLE")
    private String statutInitialCandidature;
    
    @Schema(description = "Date de création du paramètre", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dateCreation;
    
    @Schema(description = "Date de dernière modification", example = "2024-01-15T14:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dateModification;
}