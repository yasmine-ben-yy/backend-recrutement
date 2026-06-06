package com.example.recrutement.rh.service;

import com.example.recrutement.rh.dto.RhProfileRequest;
import com.example.recrutement.rh.dto.RhProfileResponse;
import com.example.recrutement.rh.entity.RhProfile;
import com.example.recrutement.rh.repository.RhProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RhProfileService {

    private final RhProfileRepository rhProfileRepository;

    public RhProfileResponse getProfile() {
        // Récupérer l'utilisateur connecté
        String email = getCurrentUserEmail();
        log.info("Recherche du profil pour l'utilisateur: {}", email);
        
        RhProfile profile = rhProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profil non trouvé pour l'email: " + email));
        
        log.info("Profil trouvé: {}", profile);
        return mapToResponse(profile);
    }

    @Transactional
    public RhProfileResponse createProfile(RhProfileRequest request) {
        String email = getCurrentUserEmail();
        log.info("Création du profil pour l'utilisateur: {}", email);
        
        // Vérifier si le profil existe déjà
        if (rhProfileRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Un profil existe déjà pour cet utilisateur");
        }
        
        RhProfile profile = RhProfile.builder()
                .email(email)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .poste(request.getPoste())
                .bio(request.getBio())
                .photo(request.getPhoto())
                .build();
        
        RhProfile savedProfile = rhProfileRepository.save(profile);
        log.info("Profil créé avec l'ID: {}", savedProfile.getId());
        
        return mapToResponse(savedProfile);
    }

    @Transactional
    public RhProfileResponse updateProfile(RhProfileRequest request) {
        String email = getCurrentUserEmail();
        log.info("Mise à jour du profil pour l'utilisateur: {}", email);
        
        RhProfile profile = rhProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profil non trouvé pour l'email: " + email));
        
        // Mise à jour des champs
        profile.setNom(request.getNom());
        profile.setPrenom(request.getPrenom());
        profile.setTelephone(request.getTelephone());
        profile.setPoste(request.getPoste());
        profile.setBio(request.getBio());
        profile.setPhoto(request.getPhoto());
        
        RhProfile updatedProfile = rhProfileRepository.save(profile);
        log.info("Profil mis à jour: {}", updatedProfile);
        
        return mapToResponse(updatedProfile);
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private RhProfileResponse mapToResponse(RhProfile profile) {
        return RhProfileResponse.builder()
                .id(profile.getId())
                .nom(profile.getNom())
                .prenom(profile.getPrenom())
                .telephone(profile.getTelephone())
                .poste(profile.getPoste())
                .bio(profile.getBio())
                .photo(profile.getPhoto())
                .email(profile.getEmail())
                .build();
    }
}