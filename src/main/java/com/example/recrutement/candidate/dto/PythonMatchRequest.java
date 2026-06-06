// com.example.recrutement.candidate.dto.PythonMatchRequest.java
package com.example.recrutement.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonMatchRequest {
    private String candidate_text;
    private String job_text;
}