package com.example.recrutement.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonAnalyzeResponse {
    private String name;
    private String email;
    private String phone;
    private String city;
    private String titre;
    private String disponibilite;
    private List<String> skills;
    private List<String> degrees;
    private int experience;
    private double processingTimeMs;
}