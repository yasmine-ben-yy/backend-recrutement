// com.example.recrutement.auth.dto.VerifyAccountResponse.java
package com.example.recrutement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAccountResponse {
    private boolean success;
    private String message;
    private String email;
}