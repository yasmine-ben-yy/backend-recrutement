package com.example.recrutement.candidate.dto;

import lombok.Data;
import java.util.List;

@Data
public class AIDetailedScoresDTO {
    private Double globalScore;
    private Double semanticScore;
    private Double skillsScore;
    private Double degreeScore;
    private Double experienceScore;
    private Double titleScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String recommendation;
    private String confidence;
}