// src/main/java/com/example/recrutement/offre/dto/TypeContratDTO.java
package com.example.recrutement.offre.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeContratDTO {
    private UUID id;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    private String description;
    private Boolean actif;
    private Integer ordre;
    private String couleur;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}