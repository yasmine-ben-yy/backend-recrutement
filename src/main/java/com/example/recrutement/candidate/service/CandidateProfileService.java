package com.example.recrutement.candidate.service;

import com.example.recrutement.candidate.dto.*;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.entity.Competence;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.candidate.repository.CompetenceRepository;
import com.example.recrutement.candidature.entity.StatutCandidature;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidateProfileService {

    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CompetenceRepository competenceRepository;

    // =============================================
    // PROFIL CANDIDAT CONNECTÉ
    // =============================================

    public CandidateProfile getByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return profileRepository.findByUser(user).orElse(null);
    }

    public CandidateProfile getCandidateProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<CandidateProfile> getAllCandidatsProfiles() {
        log.info("Récupération de tous les profils candidats");
        return profileRepository.findAll();
    }

    @Transactional
    public CandidateProfile createOrUpdateByEmail(String email, CandidateProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (user.getRole() == null) {
            user.setRole(Role.CANDIDAT);
            userRepository.save(user);
        }

        CandidateProfile profile = profileRepository.findByUser(user)
                .orElse(new CandidateProfile());

        profile.setUser(user);
        profile.setNom(dto.getNom());
        profile.setPrenom(dto.getPrenom());
        profile.setTelephone(dto.getTelephone());
        profile.setDateNaissance(dto.getDateNaissance());
        profile.setVille(dto.getVille());
        profile.setPays(dto.getPays());
        profile.setTitreProfessionnel(dto.getTitreProfessionnel());
        profile.setExperienceAnnees(dto.getExperienceAnnees());
        profile.setNiveauEtude(dto.getNiveauEtude());
        profile.setFormation(dto.getFormation());  // ✅ AJOUT DU CHAMP FORMATION
        profile.setDisponibilite(dto.getDisponibilite());

        if (dto.getCompetences() != null) {
            profile.setCompetencesFromStrings(dto.getCompetences());
        }

        profile.setLinkedinUrl(dto.getLinkedinUrl());
        profile.setPortfolioUrl(dto.getPortfolioUrl());

        if (dto.getCvPrincipalPath() != null) {
            profile.setCvPrincipalPath(dto.getCvPrincipalPath());
        }

        if (dto.getLettreMotivationPath() != null) {
            profile.setLettreMotivationPath(dto.getLettreMotivationPath());
        }

        return profileRepository.save(profile);
    }

    // =============================================
    // GESTION DES COMPÉTENCES
    // =============================================

    @Transactional
    public void addCompetenceToProfile(Long profileId, String competenceName) {
        CandidateProfile profile = getCandidateProfileById(profileId);
        
        boolean exists = competenceRepository.existsByNomAndCandidateProfileId(competenceName, profileId);
        if (!exists) {
            profile.addCompetenceByName(competenceName);
            profileRepository.save(profile);
            log.info("Compétence '{}' ajoutée au profil ID: {}", competenceName, profileId);
        } else {
            log.warn("La compétence '{}' existe déjà pour le profil ID: {}", competenceName, profileId);
        }
    }

    @Transactional
    public void removeCompetenceFromProfile(Long profileId, Long competenceId) {
        Competence competence = competenceRepository.findById(competenceId)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée"));
        
        if (competence.getCandidateProfile().getId().equals(profileId)) {
            competenceRepository.delete(competence);
            log.info("Compétence ID: {} supprimée du profil ID: {}", competenceId, profileId);
        } else {
            throw new RuntimeException("La compétence n'appartient pas à ce profil");
        }
    }

    @Transactional
    public void updateCompetences(Long profileId, List<String> competenceNames) {
        CandidateProfile profile = getCandidateProfileById(profileId);
        profile.setCompetencesFromStrings(competenceNames);
        profileRepository.save(profile);
        log.info("Compétences mises à jour pour le profil ID: {}", profileId);
    }

    // =============================================
    // LISTE CANDIDATS (RH)
    // =============================================

    @Transactional(readOnly = true)
    public List<CandidatListResponseDTO> getAllCandidats() {
        log.info("Récupération de tous les candidats");
        return profileRepository.findAll()
                .stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    private CandidatListResponseDTO convertToListDTO(CandidateProfile profile) {
        int nombreCandidatures = profile.getCandidatures() != null ? profile.getCandidatures().size() : 0;
        String statut = determineStatut(profile);

        List<String> competencesList = profile.getCompetences() != null && !profile.getCompetences().isEmpty()
                ? profile.getCompetences().stream().map(Competence::getNom).collect(Collectors.toList())
                : List.of();

        return CandidatListResponseDTO.builder()
                .id(profile.getId())
                .nom(profile.getNom())
                .prenom(profile.getPrenom())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .telephone(profile.getTelephone())
                .ville(profile.getVille())
                .pays(profile.getPays())
                .titreProfessionnel(profile.getTitreProfessionnel())
                .experienceAnnees(profile.getExperienceAnnees())
                .niveauEtude(profile.getNiveauEtude())
                .formation(profile.getFormation())  // ✅ AJOUT DU CHAMP FORMATION
                .disponibilite(profile.getDisponibilite())
                .competences(competencesList)
                .cvPrincipalPath(profile.getCvPrincipalPath())
                .statut(statut)
                .nombreCandidatures(nombreCandidatures)
                .build();
    }

    // =============================================
    // DÉTAIL CANDIDAT (RH)
    // =============================================

    @Transactional(readOnly = true)
    public CandidatDetailResponseDTO getCandidatDetailById(Long id) {
        CandidateProfile profile = getCandidateProfileById(id);
        return convertToDetailDTO(profile);
    }

    private CandidatDetailResponseDTO convertToDetailDTO(CandidateProfile profile) {
        int nombreCandidatures = profile.getCandidatures() != null ? profile.getCandidatures().size() : 0;
        String statut = determineStatut(profile);

        List<String> competencesList = profile.getCompetences() != null && !profile.getCompetences().isEmpty()
                ? profile.getCompetences().stream().map(Competence::getNom).collect(Collectors.toList())
                : List.of();

        List<CandidatureSimplifieeDTO> candidaturesDTO = profile.getCandidatures() != null
                ? profile.getCandidatures().stream()
                    .map(c -> {
                        String offreTypeContrat = null;
                        if (c.getOffre() != null && c.getOffre().getTypeContrat() != null) {
                            offreTypeContrat = c.getOffre().getTypeContrat().getNom();
                        }
                        
                        return CandidatureSimplifieeDTO.builder()
                                .id(c.getId())
                                .offreTitre(c.getOffre() != null ? c.getOffre().getTitre() : null)
                                .offreTypeContrat(offreTypeContrat)
                                .offreLocalisation(c.getOffre() != null ? c.getOffre().getLocalisation() : null)
                                .dateCandidature(c.getDateCandidature())
                                .statut(c.getStatut() != null ? c.getStatut().toString() : null)
                                .matchingScore(c.getMatchingScore())
                                .build();
                    })
                    .collect(Collectors.toList())
                : List.of();

        return CandidatDetailResponseDTO.builder()
                .id(profile.getId())
                .nom(profile.getNom())
                .prenom(profile.getPrenom())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .telephone(profile.getTelephone())
                .dateNaissance(profile.getDateNaissance())
                .ville(profile.getVille())
                .pays(profile.getPays())
                .titreProfessionnel(profile.getTitreProfessionnel())
                .experienceAnnees(profile.getExperienceAnnees())
                .niveauEtude(profile.getNiveauEtude())
                .formation(profile.getFormation())  // ✅ AJOUT DU CHAMP FORMATION
                .disponibilite(profile.getDisponibilite())
                .competences(competencesList)
                .linkedinUrl(profile.getLinkedinUrl())
                .portfolioUrl(profile.getPortfolioUrl())
                .cvPrincipalPath(profile.getCvPrincipalPath())
                .lettreMotivationPath(profile.getLettreMotivationPath())
                .statut(statut)
                .nombreCandidatures(nombreCandidatures)
                .candidatures(candidaturesDTO)
                .build();
    }

    // =============================================
    // RECHERCHE DE CANDIDATS
    // =============================================

    @Transactional(readOnly = true)
    public List<CandidatListResponseDTO> searchCandidatsByCompetence(String competence) {
        log.info("Recherche de candidats avec la compétence: {}", competence);
        
        List<CandidateProfile> profiles = profileRepository.findAll().stream()
                .filter(profile -> profile.getCompetences() != null && 
                                  profile.getCompetences().stream()
                                          .anyMatch(c -> c.getNom().toLowerCase().contains(competence.toLowerCase())))
                .collect(Collectors.toList());
        
        return profiles.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidatListResponseDTO> searchCandidatsByTitre(String titre) {
        log.info("Recherche de candidats avec le titre: {}", titre);
        
        return profileRepository.findAll().stream()
                .filter(profile -> profile.getTitreProfessionnel() != null && 
                                  profile.getTitreProfessionnel().toLowerCase().contains(titre.toLowerCase()))
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidatListResponseDTO> searchCandidatsByVille(String ville) {
        log.info("Recherche de candidats dans la ville: {}", ville);
        
        return profileRepository.findAll().stream()
                .filter(profile -> profile.getVille() != null && 
                                  profile.getVille().toLowerCase().contains(ville.toLowerCase()))
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    // =============================================
    // STATISTIQUES
    // =============================================

    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<CandidateProfile> allProfiles = profileRepository.findAll();
        
        stats.put("totalCandidats", allProfiles.size());
        
        Map<String, Long> candidatsParNiveauEtude = allProfiles.stream()
                .filter(p -> p.getNiveauEtude() != null)
                .collect(Collectors.groupingBy(CandidateProfile::getNiveauEtude, Collectors.counting()));
        stats.put("candidatsParNiveauEtude", candidatsParNiveauEtude);
        
        Map<String, Long> candidatsParFormation = allProfiles.stream()  // ✅ AJOUT STAT PAR FORMATION
                .filter(p -> p.getFormation() != null)
                .collect(Collectors.groupingBy(CandidateProfile::getFormation, Collectors.counting()));
        stats.put("candidatsParFormation", candidatsParFormation);
        
        Map<String, Long> candidatsParDisponibilite = allProfiles.stream()
                .filter(p -> p.getDisponibilite() != null)
                .collect(Collectors.groupingBy(CandidateProfile::getDisponibilite, Collectors.counting()));
        stats.put("candidatsParDisponibilite", candidatsParDisponibilite);
        
        Map<String, Long> competencesPopulaires = new HashMap<>();
        for (CandidateProfile profile : allProfiles) {
            for (Competence competence : profile.getCompetences()) {
                competencesPopulaires.merge(competence.getNom(), 1L, Long::sum);
            }
        }
        
        List<Map.Entry<String, Long>> topCompetences = competencesPopulaires.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        stats.put("topCompetences", topCompetences);
        
        Map<String, Long> candidatsParStatut = allProfiles.stream()
                .collect(Collectors.groupingBy(this::determineStatut, Collectors.counting()));
        stats.put("candidatsParStatut", candidatsParStatut);
        
        return stats;
    }

    // =============================================
    // STATUT GLOBAL
    // =============================================

    private String determineStatut(CandidateProfile profile) {
        if (profile.getCandidatures() == null || profile.getCandidatures().isEmpty()) {
            return "ACTIF";
        }
        if (profile.getCandidatures().stream().anyMatch(c -> c.getStatut() == StatutCandidature.RECRUTE)) {
            return "PLACE";
        }
        if (profile.getCandidatures().stream().anyMatch(c -> c.getStatut() == StatutCandidature.RETENUE)) {
            return "EN_ENTRETIEN";
        }
        if (profile.getCandidatures().stream().anyMatch(c -> c.getStatut() == StatutCandidature.ELIMINE)) {
            return "INACTIF";
        }
        return "ACTIF";
    }
}