package com.example.recrutement.interview.dto;

import com.example.recrutement.interview.entity.Interview;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull(message = "Le statut est requis")
    private Interview.InterviewStatus statut;
    
    private String commentaire; // Optionnel
}