package com.example.recrutement.candidate.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PythonHybridMatchRequest {
    private String candidateText;
    private String jobText;
    private List<String> candidateSkills;
    private List<String> requiredSkills;
    private Integer candidateExperience;
    private Integer requiredExperience;
    private String candidateDegree;
    private String requiredDegree;
    private String candidateTitre;
    private String jobTitre;
}