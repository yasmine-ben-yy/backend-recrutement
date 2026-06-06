// com.example.recrutement.auth.dto.RegisterRequest.java
package com.example.recrutement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'inscription d'un nouveau candidat")
public class RegisterRequest {

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Email du candidat", example = "candidat@example.com")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Schema(description = "Mot de passe", example = "password123")
    private String password;

    @NotBlank(message = "Le nom est requis")
    @Schema(description = "Nom du candidat", example = "Dupont")
    private String nom;

    @NotBlank(message = "Le prénom est requis")
    @Schema(description = "Prénom du candidat", example = "Jean")
    private String prenom;

    @Schema(description = "Téléphone (optionnel)", example = "+21612345678")
    private String telephone;

    @Schema(description = "Profil LinkedIn (optionnel)", example = "https://linkedin.com/in/jean-dupont")
    private String linkedin;

    @Schema(description = "Portfolio / Site web (optionnel)", example = "https://jean-dupont.com")
    private String portfolio;
}