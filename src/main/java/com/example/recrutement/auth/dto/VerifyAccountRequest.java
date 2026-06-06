// com.example.recrutement.auth.dto.VerifyAccountRequest.java
package com.example.recrutement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requête de vérification de compte")
public class VerifyAccountRequest {
    
    @NotBlank(message = "Le token est requis")
    @Schema(description = "Token de vérification reçu par email")
    private String token;
}