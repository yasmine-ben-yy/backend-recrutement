package com.example.recrutement.offre.controller;

import com.example.recrutement.offre.dto.OffreRequest;
import com.example.recrutement.offre.dto.OffreResponse;
import com.example.recrutement.offre.service.OffreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rh/offres")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Offres RH", description = "API pour la gestion des offres par les RH")
public class RhOffreController {

    private final OffreService offreService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB', 'RH', 'ADMIN_MB')")

    @Operation(summary = "Créer une offre")
    public ResponseEntity<OffreResponse> createOffre(
            @Valid @RequestBody OffreRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        OffreResponse response = offreService.createOffre(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Clôturer une offre")
    public ResponseEntity<Void> cloturerOffre(@PathVariable UUID id, Authentication authentication) {
        offreService.cloturerOffre(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Lister mes offres")
    public ResponseEntity<List<OffreResponse>> getMyOffres(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(offreService.getOffresByUser(userEmail));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Lister mes offres (paginated)")
    public ResponseEntity<Page<OffreResponse>> getMyOffresPaginated(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {

        String userEmail = authentication.getName();
        Page<OffreResponse> page = offreService.getOffresByUser(userEmail, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Détail d'une offre")
    public ResponseEntity<OffreResponse> getOffreById(
            @PathVariable UUID id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        return ResponseEntity.ok(offreService.getOffreByIdAndUser(id, userEmail));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Modifier une offre")
    public ResponseEntity<OffreResponse> updateOffre(
            @PathVariable UUID id,
            @Valid @RequestBody OffreRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        OffreResponse response = offreService.updateOffre(id, request, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/publier")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Publier une offre")
    public ResponseEntity<Void> publierOffre(
            @PathVariable UUID id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        offreService.publierOffre(id, userEmail);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Dupliquer une offre")
    public ResponseEntity<OffreResponse> duplicateOffre(
            @PathVariable UUID id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        OffreResponse response = offreService.duplicateOffre(id, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/archiver")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Archiver une offre")
    public ResponseEntity<Void> archiverOffre(
            @PathVariable UUID id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        offreService.archiverOffre(id, userEmail);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_RH', 'ROLE_ADMIN_MB')")
    @Operation(summary = "Supprimer une offre")
    public ResponseEntity<Void> deleteOffre(
            @PathVariable UUID id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        offreService.deleteOffre(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    
    @Operation(summary = "Rechercher mes offres")
    public ResponseEntity<List<OffreResponse>> searchOffres(
            @RequestParam String q,
            Authentication authentication) {

        String userEmail = authentication.getName();
        return ResponseEntity.ok(offreService.searchOffres(userEmail, q));
    }
}