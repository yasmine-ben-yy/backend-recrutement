package com.example.recrutement.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionDTO {
    private Long id;
    private String question;
    private String typeQuestion;
    private String competence;
    private Integer orderIndex;
}