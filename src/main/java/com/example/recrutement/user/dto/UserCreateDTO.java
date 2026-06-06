// com.example.recrutement.user.dto.UserCreateDTO.java
package com.example.recrutement.user.dto;

import com.example.recrutement.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "Email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est requis")
    private String password;
    
    @NotNull(message = "Le rôle est requis")
    private Role role;
}