package com.example.recrutement.auth.service;

import com.example.recrutement.auth.dto.*;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.security.JwtService;
import com.example.recrutement.service.IEmailService;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    // Login
    public AuthResponse authenticate(LoginRequest request) {
        log.info("Authentification de l'utilisateur: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Veuillez vérifier votre email avant de vous connecter");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .id(user.getId())
                .expiresIn(3600L)
                .build();
    }

    // Inscription avec envoi d'email de vérification
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Inscription d'un nouveau candidat: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un compte existe déjà avec cet email");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CANDIDAT);
        user.setEnabled(false);
        user.setProvider("LOCAL");
        
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiryDate(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        CandidateProfile profile = new CandidateProfile();
        profile.setUser(savedUser);
        profile.setNom(request.getNom());
        profile.setPrenom(request.getPrenom());
        profile.setTelephone(request.getTelephone());
        profile.setLinkedinUrl(request.getLinkedin());
        profile.setPortfolioUrl(request.getPortfolio());

        candidateProfileRepository.save(profile);

        // Afficher le lien dans les logs
        String verificationUrl = "http://localhost:3000/auth/verify?token=" + verificationToken;
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║     🔗 LIEN DE VÉRIFICATION POUR : {}", request.getEmail());
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║ Lien : {}", verificationUrl);
        log.info("║ Token: {}", verificationToken);
        log.info("╚════════════════════════════════════════════════════════════════╝");

        // Essayer d'envoyer l'email si activé
        sendVerificationEmail(savedUser, verificationToken);

        return RegisterResponse.builder()
                .success(true)
                .message("Inscription réussie. Veuillez vérifier votre email pour activer votre compte.")
                .email(request.getEmail())
                .build();
    }

    // Vérification du compte
    @Transactional
    public VerifyAccountResponse verifyAccount(String token) {
        log.info("Vérification du compte avec token: {}", token);

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token de vérification invalide"));

        if (user.getVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Le token de vérification a expiré");
        }

        if (user.isEnabled()) {
            throw new RuntimeException("Le compte est déjà activé");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiryDate(null);
        userRepository.save(user);

        log.info("✅ Compte vérifié avec succès: {}", user.getEmail());

        return VerifyAccountResponse.builder()
                .success(true)
                .message("Compte vérifié avec succès. Vous pouvez maintenant vous connecter.")
                .email(user.getEmail())
                .build();
    }

    // Demande de réinitialisation de mot de passe
    @Transactional
    public ForgotPasswordResponse forgotPassword(String email) {
        log.info("Demande de réinitialisation pour: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetTokenExpiryDate(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Afficher le lien dans les logs
        String resetUrl = "http://localhost:3000/auth/reset-password?token=" + resetToken;
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║   🔗 LIEN DE RÉINITIALISATION POUR : {}", email);
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║ Lien : {}", resetUrl);
        log.info("║ Token: {}", resetToken);
        log.info("╚════════════════════════════════════════════════════════════════╝");

        // Essayer d'envoyer l'email si activé
        sendResetPasswordEmail(user, resetToken);

        return ForgotPasswordResponse.builder()
                .success(true)
                .message("Un email de réinitialisation a été envoyé à votre adresse")
                .build();
    }

    // Réinitialisation du mot de passe
    @Transactional
    public ResetPasswordResponse resetPassword(String token, String newPassword, String confirmPassword) {
        log.info("Réinitialisation du mot de passe avec token: {}", token);

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token de réinitialisation invalide"));

        if (user.getResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Le token de réinitialisation a expiré");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetTokenExpiryDate(null);
        userRepository.save(user);

        log.info("✅ Mot de passe réinitialisé avec succès pour: {}", user.getEmail());

        return ResetPasswordResponse.builder()
                .success(true)
                .message("Mot de passe réinitialisé avec succès. Vous pouvez maintenant vous connecter.")
                .build();
    }

    // Renvoyer l'email de vérification
    @Transactional
    public ResendVerificationResponse resendVerificationEmail(String email) {
        log.info("Renvoyer l'email de vérification pour: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        if (user.isEnabled()) {
            throw new RuntimeException("Le compte est déjà activé");
        }

        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiryDate(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String verificationUrl = "http://localhost:3000/auth/verify?token=" + newToken;
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║   🔗 NOUVEAU LIEN DE VÉRIFICATION POUR : {}", email);
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║ Lien : {}", verificationUrl);
        log.info("║ Token: {}", newToken);
        log.info("╚════════════════════════════════════════════════════════════════╝");

        sendVerificationEmail(user, newToken);

        return ResendVerificationResponse.builder()
                .success(true)
                .message("Un nouvel email de vérification a été envoyé")
                .build();
    }

    // Envoi d'email de vérification
    private void sendVerificationEmail(User user, String token) {
        if (!emailEnabled) {
            log.info("📧 Email désactivé - Pas d'envoi pour: {}", user.getEmail());
            return;
        }
        
        try {
            String verificationUrl = "http://localhost:3000/auth/verify?token=" + token;
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", user.getEmail().split("@")[0]);
            variables.put("verificationUrl", verificationUrl);
            variables.put("expirationHours", 24);
            
            String htmlContent = emailService.renderTemplate("email/verify-account", variables);
            emailService.sendEmail(user.getEmail(), "Vérifiez votre compte", htmlContent);
            
            log.info("✅ Email de vérification envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'email de vérification: {}", e.getMessage());
        }
    }

    // Envoi d'email de réinitialisation
    private void sendResetPasswordEmail(User user, String resetToken) {
        if (!emailEnabled) {
            log.info("📧 Email désactivé - Pas d'envoi pour: {}", user.getEmail());
            return;
        }
        
        try {
            String resetUrl = "http://localhost:3000/auth/reset-password?token=" + resetToken;
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("prenom", user.getEmail().split("@")[0]);
            variables.put("resetUrl", resetUrl);
            variables.put("expirationHours", 1);
            
            String htmlContent = emailService.renderTemplate("email/reset-password", variables);
            emailService.sendEmail(user.getEmail(), "Réinitialisation de votre mot de passe", htmlContent);
            
            log.info("✅ Email de réinitialisation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage());
        }
    }
}