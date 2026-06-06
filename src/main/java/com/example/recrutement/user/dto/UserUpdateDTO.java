// com.example.recrutement.user.dto.UserUpdateDTO.java
package com.example.recrutement.user.dto;

import com.example.recrutement.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private Role role;
    private Boolean enabled;
    private String password;
}