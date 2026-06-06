// com.example.recrutement.user.service.UserService.java
package com.example.recrutement.user.service;

import com.example.recrutement.user.dto.UserCreateDTO;
import com.example.recrutement.user.dto.UserDTO;
import com.example.recrutement.user.dto.UserUpdateDTO;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO dto) {
        log.info("📝 Création d'un nouvel utilisateur: {}", dto.getEmail());
        
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.error("❌ Email déjà utilisé: {}", dto.getEmail());
            throw new RuntimeException("Email déjà utilisé");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .enabled(true)
                .provider("LOCAL")
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("✅ Utilisateur créé avec succès: {} - Rôle: {}", savedUser.getEmail(), savedUser.getRole());
        
        return convertToDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO dto) {
        log.info("📝 Mise à jour de l'utilisateur: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("✅ Utilisateur mis à jour: {}", savedUser.getEmail());
        
        return convertToDTO(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("🗑️ Suppression de l'utilisateur: {}", id);
        userRepository.deleteById(id);
        log.info("✅ Utilisateur supprimé: {}", id);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}