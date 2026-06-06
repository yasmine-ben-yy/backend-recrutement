// com.example.recrutement.ai.dto.AiSummaryResponse.java
package com.example.recrutement.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiSummaryResponse {
    private String summary;
    private List<String> mainSkills;
    private int experienceYears;
    private int compatibilityScore;
    private List<String> recommendations;
}