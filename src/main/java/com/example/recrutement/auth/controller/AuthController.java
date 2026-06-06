// com.example.recrutement.auth.controller.AuthController.java
package com.example.recrutement.auth.controller;

import com.example.recrutement.auth.dto.*;
import com.example.recrutement.auth.service.CustomOAuth2UserService;
import com.example.recrutement.security.JwtService;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Endpoints pour l'authentification et la gestion des comptes")
public class AuthController {

    private final AuthService authService;
    private final CustomOAuth2UserService customOAuth2UserService;  // ✅ Ajouter
    private final JwtService jwtService;  // ✅ Ajouter


    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Inscrit un nouveau candidat et envoie un email de vérification")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify")
    @Operation(summary = "Vérification du compte", description = "Vérifie le compte avec le token reçu par email")
    public ResponseEntity<VerifyAccountResponse> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        return ResponseEntity.ok(authService.verifyAccount(request.getToken()));
    }

    @GetMapping("/verify")
    @Operation(summary = "Vérification du compte (GET)", description = "Vérifie le compte avec le token dans l'URL")
    public ResponseEntity<VerifyAccountResponse> verifyAccountGet(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyAccount(token));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Mot de passe oublié", description = "Envoie un email de réinitialisation")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialisation du mot de passe", description = "Réinitialise le mot de passe avec le token")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(
            request.getToken(), 
            request.getNewPassword(), 
            request.getConfirmPassword()
        ));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Renvoyer l'email de vérification", description = "Envoie un nouvel email de vérification")
    public ResponseEntity<ResendVerificationResponse> resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.resendVerificationEmail(request.getEmail()));
    }
    @PostMapping("/oauth2/google")
    @Operation(summary = "OAuth2 Google", description = "Login ou inscription via Google OAuth2")
    public ResponseEntity<AuthResponse> handleGoogleOAuth(@RequestBody OAuth2Request request) {
        log.info("🔐 Processing Google OAuth2 for email: {}", request.getEmail());
        
        // Construire l'OAuth2UserInfo
        OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                .id(request.getProviderId())
                .email(request.getEmail())
                .name(request.getName())
                .givenName(request.getGivenName())
                .familyName(request.getFamilyName())
                .pictureUrl(request.getPictureUrl())
                .provider("GOOGLE")
                .build();
        
        // Traiter l'utilisateur (création ou mise à jour)
        User user = customOAuth2UserService.processOAuth2User(userInfo);
        
        // Générer le token JWT
        String token = jwtService.generateToken(user);
        
        // Construire la réponse
        AuthResponse response = AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role("ROLE_" + user.getRole().name())
                .token(token)
                .tokenType("Bearer")
                .expiresIn((long) 86400) // 24 heures
                .build();
        
        log.info("✅ JWT généré pour OAuth2 user: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    // ✅ DTO interne pour la requête OAuth2
    public static class OAuth2Request {
        private String email;
        private String name;
        private String givenName;
        private String familyName;
        private String providerId;
        private String pictureUrl;
        
        // Getters et setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }
        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }
        public String getProviderId() { return providerId; }
        public void setProviderId(String providerId) { this.providerId = providerId; }
        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
    
}