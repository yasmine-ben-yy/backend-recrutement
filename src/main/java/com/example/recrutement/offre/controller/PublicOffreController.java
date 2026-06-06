// com.example.recrutement.offre.controller.PublicOffreController.java
package com.example.recrutement.offre.controller;

import com.example.recrutement.offre.dto.OffreResponse;
import com.example.recrutement.offre.dto.OffreSimpleResponse;
import com.example.recrutement.offre.service.OffreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/offres")
@RequiredArgsConstructor
@Tag(name = "Offres Publiques", description = "API publique pour consulter les offres")
@CrossOrigin(origins = "*")
public class PublicOffreController {
    
    private final OffreService offreService;

    @GetMapping
    public ResponseEntity<List<OffreSimpleResponse>> getOffresPubliees(
            @RequestParam(required = false) String motCle,
            @RequestParam(required = false) String localisation,
            @RequestParam(required = false) String typeContrat,
            @RequestParam(required = false) String domaine
    ) {
        return ResponseEntity.ok(
            offreService.getOffresAvecFiltres(motCle, localisation, typeContrat, domaine)
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une offre")
    public ResponseEntity<OffreResponse> getOffreById(@PathVariable UUID id) {
        return ResponseEntity.ok(offreService.getOffrePubliqueById(id));
    }
}