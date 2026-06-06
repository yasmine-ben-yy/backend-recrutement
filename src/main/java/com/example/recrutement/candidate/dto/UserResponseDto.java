package com.example.recrutement.candidate.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String role="CANDIDAT";   // ici tu peux mettre "CANDIDAT"
    private boolean enabled;
}