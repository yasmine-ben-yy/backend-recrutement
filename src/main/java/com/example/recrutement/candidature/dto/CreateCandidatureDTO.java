package com.example.recrutement.candidature.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCandidatureDTO {
    @NotNull
    private Long candidateId;
    
    @NotNull
    private UUID offreId;
    
    private String lettreMotivationPath;
}