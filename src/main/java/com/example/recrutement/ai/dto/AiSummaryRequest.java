// com.example.recrutement.ai.dto.AiSummaryRequest.java
package com.example.recrutement.ai.dto;

import lombok.Data;

@Data
public class AiSummaryRequest {
    private String cvText;
    private String jobText;
}