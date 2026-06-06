// com.example.recrutement.auth.service.CandidateAuthService.java
package com.example.recrutement.auth.service;

import com.example.recrutement.auth.dto.AuthResponse;
import com.example.recrutement.auth.dto.LoginRequest;
import com.example.recrutement.auth.dto.RegisterRequest;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.repository.UserRepository;
import com.example.recrutement.security.JwtService;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Login corrigé
    public AuthResponse authenticate(LoginRequest request) {
        log.info("Authentification de l'utilisateur: {}", request.getEmail());

        // 1. Authentifier avec Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Récupérer le UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 3. Récupérer l'entité User depuis la base de données
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 4. Générer le token
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(3600L)
                .build();
    }

    // ✅ Inscription (inchangée)
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Inscription d'un nouveau candidat: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un compte existe déjà avec cet email");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CANDIDAT);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        CandidateProfile profile = new CandidateProfile();
        profile.setUser(savedUser);
        profile.setNom(request.getNom());
        profile.setPrenom(request.getPrenom());
        profile.setTelephone(request.getTelephone());
        profile.setLinkedinUrl(request.getLinkedin());
        profile.setPortfolioUrl(request.getPortfolio());

        candidateProfileRepository.save(profile);

        String token = jwtService.generateToken(savedUser);

        log.info("Candidat inscrit avec succès: {}", request.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(request.getEmail())
                .role("CANDIDAT")
                .expiresIn(3600L)
                .build();
    }
}