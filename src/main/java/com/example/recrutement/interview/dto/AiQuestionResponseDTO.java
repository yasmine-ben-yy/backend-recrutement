// com.example.recrutement.interview.dto/AiQuestionResponseDTO.java
package com.example.recrutement.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionResponseDTO {
    private Long id;
    private Long interviewId;
    private List<String> questions;
    private String candidateLevel;
    private Integer questionCount;
}