// com.example.recrutement.candidate.dto.PythonParseCVResponse.java
package com.example.recrutement.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonParseCVResponse {
    private Boolean success;
    private PythonCVData data;
    private String error;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PythonCVData {
        private String name;
        private String email;
        private String phone;
        private List<String> skills;
        private List<String> degrees;
        private Integer experience;
        private String raw_text;
    }
}