package com.example.recrutement.candidature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageDTO {
    private String stage;
    private String label;
    private String badgeStyle;
    private Integer count;
}