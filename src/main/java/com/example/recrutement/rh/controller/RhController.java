package com.example.recrutement.rh.controller;

import com.example.recrutement.rh.dto.RhProfileRequest;
import com.example.recrutement.rh.dto.RhProfileResponse;
import com.example.recrutement.rh.service.RhProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rh")
@RequiredArgsConstructor
@Tag(name = "RH", description = "Endpoints pour la gestion des profils RH")
@CrossOrigin(origins = "http://localhost:3000") // Ajoutez l'URL de votre frontend
public class RhController {

    private final RhProfileService rhProfileService;

    @GetMapping("/profile")
    @Operation(summary = "Récupérer le profil RH", description = "Retourne le profil de l'utilisateur RH connecté")
    public ResponseEntity<?> getProfile() {
        try {
            log.info("Tentative de récupération du profil RH");
            RhProfileResponse profile = rhProfileService.getProfile();
            log.info("Profil récupéré avec succès: {}", profile);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération du profil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Profil non trouvé", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur inattendue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur serveur", e.getMessage()));
        }
    }

    @PostMapping("/profile")
    @Operation(summary = "Créer un profil RH", description = "Crée un nouveau profil pour l'utilisateur RH connecté")
    public ResponseEntity<?> createProfile(@RequestBody RhProfileRequest request) {
        try {
            log.info("Tentative de création de profil: {}", request);
            RhProfileResponse profile = rhProfileService.createProfile(request);
            log.info("Profil créé avec succès: {}", profile);
            return new ResponseEntity<>(profile, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création du profil: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Erreur de validation", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "Mettre à jour le profil RH", description = "Met à jour le profil de l'utilisateur RH connecté")
    public ResponseEntity<?> updateProfile(@RequestBody RhProfileRequest request) {
        try {
            log.info("Tentative de mise à jour de profil: {}", request);
            RhProfileResponse profile = rhProfileService.updateProfile(request);
            log.info("Profil mis à jour avec succès: {}", profile);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour du profil: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Erreur de mise à jour", e.getMessage()));
        }
    }
    
    // Classe interne pour les réponses d'erreur
    static class ErrorResponse {
        private String error;
        private String message;
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        
        // Getters et setters
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}