// com.example.recrutement.ai.dto.AiQuestionsResponse.java
package com.example.recrutement.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiQuestionsResponse {
    private List<String> questions;
}