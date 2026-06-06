package com.example.recrutement.candidature.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueNoteDTO {
    private Long id;
    private LocalDateTime date;
    private String auteur;
    private String contenu;
}