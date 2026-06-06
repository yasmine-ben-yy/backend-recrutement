// com.example.recrutement.candidature.dto.CandidatureRapideRequest.java
package com.example.recrutement.candidature.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CandidatureRapideRequest {
    
    @NotBlank(message = "L'ID de l'offre est requis")
    private String offreId;
    
    @Email(message = "Email invalide")
    private String email;
    
    private String nom;
    private String prenom;
    private String telephone;
    
    private MultipartFile cv;  // ✅ Fichier CV
    private String lettreMotivation;
}