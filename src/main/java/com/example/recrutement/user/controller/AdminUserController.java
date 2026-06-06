// com.example.recrutement.user.controller.AdminUserController.java
package com.example.recrutement.user.controller;

import com.example.recrutement.user.dto.UserCreateDTO;
import com.example.recrutement.user.dto.UserDTO;
import com.example.recrutement.user.dto.UserUpdateDTO;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Utilisateurs", description = "Gestion des utilisateurs")
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN_MB')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Liste tous les utilisateurs")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("📋 GET /api/admin/users - Récupération de tous les utilisateurs");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Liste les utilisateurs par rôle")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Role role) {
        log.info("📋 GET /api/admin/users/role/{} - Récupération des utilisateurs par rôle", role);
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un utilisateur par son ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("📋 GET /api/admin/users/{} - Récupération de l'utilisateur", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "Crée un nouvel utilisateur")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        log.info("📝 POST /api/admin/users - Création d'un utilisateur: {}", dto.getEmail());
        try {
            UserDTO created = userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifie un utilisateur")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        log.info("📝 PUT /api/admin/users/{} - Mise à jour de l'utilisateur", id);
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("🗑️ DELETE /api/admin/users/{} - Suppression de l'utilisateur", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}