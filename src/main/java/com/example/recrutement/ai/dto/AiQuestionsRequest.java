// com.example.recrutement.ai.dto.AiQuestionsRequest.java
package com.example.recrutement.ai.dto;

import lombok.Data;

@Data
public class AiQuestionsRequest {
    private String cvText;
    private String jobText;
    private String candidateLevel; // junior, intermediate, senior
    private int questionCount = 5;
}