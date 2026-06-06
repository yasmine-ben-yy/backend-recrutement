// com.example.recrutement.publics.PublicController.java
package com.example.recrutement.publics;

import com.example.recrutement.offre.dto.DomaineDTO;
import com.example.recrutement.offre.dto.TypeContratDTO;
import com.example.recrutement.offre.service.DomaineService;
import com.example.recrutement.offre.service.TypeContratService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "API publiques accessibles sans authentification")
public class PublicController {

    private final DomaineService domaineService;
    private final TypeContratService typeContratService;

    @GetMapping("/domaines")
    @Operation(summary = "Liste des domaines actifs")
    public ResponseEntity<List<DomaineDTO>> getDomaines() {
        return ResponseEntity.ok(domaineService.getAllDomaines());
    }

    @GetMapping("/types-contrat")
    @Operation(summary = "Liste des types de contrat actifs")
    public ResponseEntity<List<TypeContratDTO>> getTypesContrat() {
        return ResponseEntity.ok(typeContratService.getAllTypesContrat());
    }
    @GetMapping("/matching/test")
    public ResponseEntity<Map<String, String>> testMatching() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Matching API is working");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}