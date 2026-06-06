package com.example.recrutement.offre.controller;

import com.example.recrutement.offre.dto.DomaineDTO;
import com.example.recrutement.offre.service.DomaineService;
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
@RequestMapping("/api/admin/domaines")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Domaines", description = "Gestion des domaines d'activité (admin/RH)")
public class DomaineController {

    private final DomaineService domaineService;

    @GetMapping("/public")
    @Operation(summary = "Liste des domaines actifs")
    public ResponseEntity<List<DomaineDTO>> getDomainesActifs() {
        return ResponseEntity.ok(domaineService.getAllDomaines());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Liste complète des domaines (admin/RH)")
    public ResponseEntity<List<DomaineDTO>> getAllDomaines() {
        return ResponseEntity.ok(domaineService.getAllDomainesAdmin());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Détail d'un domaine")
    public ResponseEntity<DomaineDTO> getDomaineById(@PathVariable UUID id) {
        return ResponseEntity.ok(domaineService.getDomaineById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Créer un domaine")
    public ResponseEntity<DomaineDTO> createDomaine(@Valid @RequestBody DomaineDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(domaineService.createDomaine(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Modifier un domaine")
    public ResponseEntity<DomaineDTO> updateDomaine(
            @PathVariable UUID id,
            @Valid @RequestBody DomaineDTO dto) {
        return ResponseEntity.ok(domaineService.updateDomaine(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")    @Operation(summary = "Supprimer un domaine")
    public ResponseEntity<Void> deleteDomaine(@PathVariable UUID id) {
        domaineService.deleteDomaine(id);
        return ResponseEntity.noContent().build();
    }
}