// com.example.recrutement.auth.dto.ResetPasswordRequest.java
package com.example.recrutement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Requête de réinitialisation de mot de passe")
public class ResetPasswordRequest {
    
    @NotBlank(message = "Le token est requis")
    @Schema(description = "Token de réinitialisation reçu par email")
    private String token;
    
    @NotBlank(message = "Le nouveau mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Schema(description = "Nouveau mot de passe")
    private String newPassword;
    
    @NotBlank(message = "La confirmation du mot de passe est requise")
    @Schema(description = "Confirmation du nouveau mot de passe")
    private String confirmPassword;
}