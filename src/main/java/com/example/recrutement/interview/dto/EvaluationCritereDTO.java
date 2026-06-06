package com.example.recrutement.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCritereDTO {
    private String critere;
    private String label;
    private Integer note;
    private String commentaire;
}