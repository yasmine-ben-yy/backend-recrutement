// com.example.recrutement.auth.dto.ForgotPasswordResponse.java
package com.example.recrutement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    private boolean success;
    private String message;
}