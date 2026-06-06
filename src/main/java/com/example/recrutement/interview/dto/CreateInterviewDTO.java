// com.example.recrutement.interview.dto.CreateInterviewDTO.java
package com.example.recrutement.interview.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CreateInterviewDTO {

    @NotNull(message = "L'ID de la candidature est requis")
    private Long candidatureId;

    @NotNull(message = "Le type d'entretien est requis")
    private String type;

    @NotNull(message = "La date de l'entretien est requise")
    private LocalDateTime dateEntretien;

    @Min(value = 15, message = "La durée minimum est de 15 minutes")
    private Integer dureeMinutes = 60;

    private String lieu;
    private String meetingLink;
    private String notes;
}