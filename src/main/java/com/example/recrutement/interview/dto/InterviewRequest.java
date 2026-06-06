// com.example.recrutement.interview.dto.InterviewRequest.java
package com.example.recrutement.interview.dto;

import com.example.recrutement.interview.entity.Interview;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRequest {

    @NotNull(message = "L'ID de la candidature est requis")
    private Long candidatureId;

    @NotNull(message = "Le type d'entretien est requis")
    private Interview.InterviewType type;

    @NotNull(message = "La date est requise")
    @Future(message = "La date doit être dans le futur")
    private LocalDateTime date;

    @Min(value = 15, message = "La durée minimum est de 15 minutes")
    private Integer duree = 60;

    private String lieu;
    private String meetingLink;
    private String notes;
}