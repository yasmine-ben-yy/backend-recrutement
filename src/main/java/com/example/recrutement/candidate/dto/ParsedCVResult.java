package com.example.recrutement.candidate.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedCVResult {
    private List<String> competences;
    private int experienceAnnees;
    private String niveauEtude;
    private String titreProfessionnel;
    private String disponibilite;
    
    public ParsedCVResult() {
        this.competences = new ArrayList<>();
        this.experienceAnnees = 0;
        this.niveauEtude = "Non spécifié";
        this.titreProfessionnel = "Non spécifié";
        this.disponibilite = "Non spécifiée";
    }
}