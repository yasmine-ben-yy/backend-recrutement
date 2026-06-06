// com.example.recrutement.auth.dto.ResetPasswordResponse.java
package com.example.recrutement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {
    private boolean success;
    private String message;
}