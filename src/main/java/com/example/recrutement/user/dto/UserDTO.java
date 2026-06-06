// com.example.recrutement.user.dto.UserDTO.java
package com.example.recrutement.user.dto;

import com.example.recrutement.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}