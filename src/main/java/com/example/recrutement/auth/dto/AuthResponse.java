package com.example.recrutement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'authentification avec token JWT")
public class AuthResponse {

    @Schema(description = "Token JWT d'accès", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Type du token", example = "Bearer")
    private String tokenType;

    @Schema(description = "Email de l'utilisateur authentifié", example = "RH@example.com")
    private String email;
    @Schema(description = "ID de l'utilisateur", example = "1")
    private Long id;
    @Schema(description = "Rôle de l'utilisateur", example = "ROLE_RH")
    private String role;

    @Schema(description = "Durée de validité du token en secondes", example = "3600")
    private Long expiresIn;
}