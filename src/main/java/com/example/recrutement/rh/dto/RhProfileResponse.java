package com.example.recrutement.rh.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RhProfileResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String poste;
    private String bio;
    private String photo;
    private String email;

}