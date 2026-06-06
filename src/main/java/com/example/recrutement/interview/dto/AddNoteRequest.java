package com.example.recrutement.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddNoteRequest {
    @NotBlank(message = "Le contenu de la note est requis")
    private String contenu;
}