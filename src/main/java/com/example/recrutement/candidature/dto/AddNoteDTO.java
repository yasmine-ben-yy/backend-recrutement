package com.example.recrutement.candidature.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddNoteDTO {
    @NotBlank(message = "Le contenu de la note est requis")
    private String contenu;
}