package com.example.recrutement.offre.controller;

import com.example.recrutement.offre.dto.TypeContratDTO;
import com.example.recrutement.offre.service.TypeContratService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/types-contrat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Types de contrat", description = "Gestion des types de contrat (admin/RH)")
public class TypeContratController {

    private final TypeContratService typeContratService;

    @GetMapping("/public")
    @Operation(summary = "Liste des types de contrat actifs")
    public ResponseEntity<List<TypeContratDTO>> getTypesContratActifs() {
        return ResponseEntity.ok(typeContratService.getAllTypesContrat());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Liste complète des types de contrat (admin/RH)")
    public ResponseEntity<List<TypeContratDTO>> getAllTypesContrat() {
        return ResponseEntity.ok(typeContratService.getAllTypesContratAdmin());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Détail d'un type de contrat")
    public ResponseEntity<TypeContratDTO> getTypeContratById(@PathVariable UUID id) {
        return ResponseEntity.ok(typeContratService.getTypeContratById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Créer un type de contrat")
    public ResponseEntity<TypeContratDTO> createTypeContrat(@Valid @RequestBody TypeContratDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(typeContratService.createTypeContrat(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Modifier un type de contrat")
    public ResponseEntity<TypeContratDTO> updateTypeContrat(
            @PathVariable UUID id,
            @Valid @RequestBody TypeContratDTO dto) {
        return ResponseEntity.ok(typeContratService.updateTypeContrat(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Supprimer un type de contrat")
    public ResponseEntity<Void> deleteTypeContrat(@PathVariable UUID id) {
        typeContratService.deleteTypeContrat(id);
        return ResponseEntity.noContent().build();
    }
}