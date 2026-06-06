// com.example.recrutement.auth.dto.ForgotPasswordRequest.java
package com.example.recrutement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requête de demande de réinitialisation de mot de passe")
public class ForgotPasswordRequest {
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Email du compte", example = "user@example.com")
    private String email;
}