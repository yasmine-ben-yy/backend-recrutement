// com.example.recrutement.auth.dto.ResendVerificationResponse.java
package com.example.recrutement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationResponse {
    private boolean success;
    private String message;
}