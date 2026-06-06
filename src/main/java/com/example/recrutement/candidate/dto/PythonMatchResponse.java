package com.example.recrutement.candidate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PythonMatchResponse {
    @JsonProperty("global_score")
    private Double globalScore;
    @JsonProperty("semantic_score")
    private Double semanticScore;
    @JsonProperty("skills_score")
    private Double skillsScore;
    @JsonProperty("degree_score")
    private Double degreeScore;
    @JsonProperty("experience_score")
    private Double experienceScore;
    @JsonProperty("title_score")
    private Double titleScore;
    @JsonProperty("matched_skills")
    private List<String> matchedSkills;
    @JsonProperty("missing_skills")
    private List<String> missingSkills;
    @JsonProperty("recommendation")
    private String recommendation;
    @JsonProperty("confidence")
    private String confidence;
}