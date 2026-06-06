package com.example.recrutement.candidature.service;

import com.example.recrutement.candidature.dto.CandidatureRapideRequest;
import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.entity.StatutCandidature;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.repository.OffreRepository;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.entity.Competence;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.candidate.repository.CompetenceRepository;
import com.example.recrutement.candidate.service.AIMatchingService;
import com.example.recrutement.candidate.service.CVParsingService;
import com.example.recrutement.candidate.dto.PythonAnalyzeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CandidatureRapideService {

    private final CandidatureRepository candidatureRepository;
    private final OffreRepository offreRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final CompetenceRepository competenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AIMatchingService aiMatchingService;
    private final CVParsingService cvParsingService;

    @Value("${app.upload.cv.dir:uploads/cvs}")
    private String uploadCvDir;

    public CandidatureRapideService(
            CandidatureRepository candidatureRepository,
            OffreRepository offreRepository,
            UserRepository userRepository,
            CandidateProfileRepository candidateProfileRepository,
            CompetenceRepository competenceRepository,
            PasswordEncoder passwordEncoder,
            AIMatchingService aiMatchingService,
            CVParsingService cvParsingService) {
        this.candidatureRepository = candidatureRepository;
        this.offreRepository = offreRepository;
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.competenceRepository = competenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.aiMatchingService = aiMatchingService;
        this.cvParsingService = cvParsingService;
    }

    @Transactional(rollbackFor = Exception.class)
    public Candidature postulerSansCompte(CandidatureRapideRequest request) {
        log.info("========== DÉBUT CANDIDATURE RAPIDE ==========");
        
        // 1. Récupération de l'offre
        OffreEmploi offre = offreRepository.findById(UUID.fromString(request.getOffreId()))
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));
        
        // 2. Analyse du CV (TOUJOURS en priorité)
        PythonAnalyzeResponse cvInfo = null;
        if (request.getCv() != null && !request.getCv().isEmpty()) {
            log.info("📄 Analyse du CV: {}", request.getCv().getOriginalFilename());
            cvInfo = cvParsingService.analyzeCV(request.getCv());
            
            if (cvInfo != null) {
                log.info("✅ CV analysé avec succès:");
                log.info("   - Nom: {}", cvInfo.getName());
                log.info("   - Titre: {}", cvInfo.getTitre());
                log.info("   - Email: {}", cvInfo.getEmail());
                log.info("   - Téléphone: {}", cvInfo.getPhone());
                log.info("   - Ville: {}", cvInfo.getCity());
                log.info("   - Disponibilité: {}", cvInfo.getDisponibilite());
                log.info("   - Expérience: {} ans", cvInfo.getExperience());
                log.info("   - Compétences: {}", cvInfo.getSkills());
                log.info("   - Diplômes: {}", cvInfo.getDegrees());
            }
        }
        
        // 3. Détermination de l'email (PRIORITÉ AU CV)
        String email = null;
        if (cvInfo != null && cvInfo.getEmail() != null && !cvInfo.getEmail().isBlank()) {
            email = cvInfo.getEmail();
            log.info("📧 Email extrait du CV: {}", email);
        } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
            email = request.getEmail();
            log.info("📧 Email du formulaire: {}", email);
        }
        
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email requis (CV ou formulaire)");
        }
        
        // 4. Création ou récupération de l'utilisateur
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("👤 Utilisateur existant: {}", email);
        } else {
            String tempPassword = UUID.randomUUID().toString().substring(0, 12);
            user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setRole(Role.CANDIDAT);
            user.setEnabled(true);
            user.setProvider("LOCAL");
            user = userRepository.save(user);
            log.info("📧 Nouvel utilisateur créé: {} (mot de passe temporaire: {})", email, tempPassword);
        }
        
        // 5. Création ou mise à jour du profil
        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElse(CandidateProfile.builder().user(user).build());
        
        // ============================================
        // PRIORITÉ AU CV (PUIS FORMULAIRE SI CV VIDE)
        // ============================================
        
        // --- NOM & PRÉNOM (PRIORITÉ AU CV) ---
        if (cvInfo != null && cvInfo.getName() != null && !cvInfo.getName().isBlank()) {
            String[] parts = cvInfo.getName().trim().split("\\s+");
            if (parts.length >= 2) {
                profile.setPrenom(parts[0]);
                profile.setNom(parts[parts.length - 1]);
                log.info("  ✓ Prénom/Nom (extrait CV): {} / {}", parts[0], parts[parts.length - 1]);
            } else if (parts.length == 1) {
                profile.setNom(parts[0]);
                log.info("  ✓ Nom (extrait CV): {}", parts[0]);
            }
        } else {
            // Fallback sur le formulaire
            if (request.getPrenom() != null && !request.getPrenom().isBlank()) {
                profile.setPrenom(request.getPrenom());
                log.info("  ✓ Prénom (formulaire): {}", request.getPrenom());
            }
            if (request.getNom() != null && !request.getNom().isBlank()) {
                profile.setNom(request.getNom());
                log.info("  ✓ Nom (formulaire): {}", request.getNom());
            }
        }
        
        // --- EMAIL (déjà défini) ---
        profile.setEmail(email);
        log.info("  ✓ Email: {}", email);
        
        // --- TÉLÉPHONE (PRIORITÉ AU CV) ---
        if (cvInfo != null && cvInfo.getPhone() != null && !cvInfo.getPhone().isBlank()) {
            profile.setTelephone(cvInfo.getPhone());
            log.info("  ✓ Téléphone (extrait CV): {}", cvInfo.getPhone());
        } else if (request.getTelephone() != null && !request.getTelephone().isBlank()) {
            profile.setTelephone(request.getTelephone());
            log.info("  ✓ Téléphone (formulaire): {}", request.getTelephone());
        }
        
        // --- VILLE (extrait CV) ---
        if (cvInfo != null && cvInfo.getCity() != null && !cvInfo.getCity().isBlank()) {
            profile.setVille(cvInfo.getCity());
            log.info("  ✓ Ville (extrait CV): {}", cvInfo.getCity());
        }
        
        // --- TITRE PROFESSIONNEL (extrait CV) ---
        if (cvInfo != null && cvInfo.getTitre() != null && !cvInfo.getTitre().isBlank()) {
            profile.setTitreProfessionnel(cvInfo.getTitre());
            log.info("  ✓ Titre (extrait CV): {}", cvInfo.getTitre());
        } else if (cvInfo != null && cvInfo.getSkills() != null && !cvInfo.getSkills().isEmpty()) {
            profile.setTitreProfessionnel(cvInfo.getSkills().get(0));
            log.info("  ✓ Titre (défaut): {}", cvInfo.getSkills().get(0));
        }
        
        // --- DISPONIBILITÉ (extrait CV) ---
        if (cvInfo != null && cvInfo.getDisponibilite() != null && !cvInfo.getDisponibilite().isBlank()) {
            profile.setDisponibilite(cvInfo.getDisponibilite());
            log.info("  ✓ Disponibilité (extrait CV): {}", cvInfo.getDisponibilite());
        } else {
            profile.setDisponibilite("Immédiate");
            log.info("  ✓ Disponibilité (défaut): Immédiate");
        }
        
        // --- EXPÉRIENCE ---
        if (cvInfo != null && cvInfo.getExperience() > 0) {
            profile.setExperienceAnnees(cvInfo.getExperience());
            log.info("  ✓ Expérience (extrait CV): {} ans", cvInfo.getExperience());
        } else {
            profile.setExperienceAnnees(0);
            log.info("  ✓ Expérience (défaut): 0 ans");
        }
        
        // --- DIPLÔME ---
        if (cvInfo != null && cvInfo.getDegrees() != null && !cvInfo.getDegrees().isEmpty()) {
            String education = String.join(", ", cvInfo.getDegrees());
            profile.setNiveauEtude(education);
            log.info("  ✓ Diplôme (extrait CV): {}", education);
        }
        
        // Première sauvegarde du profil
        profile = candidateProfileRepository.save(profile);
        log.info("  ✓ Profil sauvegardé avec ID: {}", profile.getId());
        
        // --- COMPÉTENCES ---
        if (cvInfo != null && cvInfo.getSkills() != null && !cvInfo.getSkills().isEmpty()) {
            // Supprimer les anciennes compétences
            if (profile.getCompetences() != null && !profile.getCompetences().isEmpty()) {
                competenceRepository.deleteAll(profile.getCompetences());
                profile.getCompetences().clear();
            }
            
            // Ajouter les nouvelles compétences
            for (String skillName : cvInfo.getSkills()) {
                if (skillName != null && !skillName.trim().isEmpty()) {
                    Competence competence = Competence.builder()
                            .nom(skillName.trim())
                            .niveau("Intermédiaire")
                            .candidateProfile(profile)
                            .build();
                    competence = competenceRepository.save(competence);
                    profile.getCompetences().add(competence);
                    log.info("  ✓ Compétence ajoutée: {}", skillName);
                }
            }
            log.info("  📚 {} compétences enregistrées", cvInfo.getSkills().size());
        }
        
        // Sauvegarde finale du profil
        profile = candidateProfileRepository.save(profile);
        
        // --- CV FICHIER ---
        if (request.getCv() != null && !request.getCv().isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadCvDir).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);
                String filename = "cv_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
                Path filePath = uploadPath.resolve(filename);
                Files.copy(request.getCv().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                profile.setCvPrincipalPath("/uploads/cvs/" + filename);
                profile = candidateProfileRepository.save(profile);
                log.info("  💾 CV sauvegardé: {}", filename);
            } catch (IOException e) {
                log.error("  ❌ Erreur sauvegarde CV: {}", e.getMessage());
            }
        }
        
        // 6. Calcul du score de matching
        double score = aiMatchingService.calculateMatchingScore(profile, offre);
        log.info("🤖 Score IA: {}%", Math.round(score));
        
        // 7. Création de la candidature
        Candidature candidature = Candidature.builder()
                .candidate(profile)
                .offre(offre)
                .dateCandidature(LocalDateTime.now())
                .statut(StatutCandidature.EN_ATTENTE)
                .matchingScore(score)
                .emailSent(false)
                .emailRetryCount(0)
                .build();
        
        candidature = candidatureRepository.save(candidature);
        
        log.info("==========================================");
        log.info("📊 RÉCAPITULATIF FINAL:");
        log.info("  - ID Profil: {}", profile.getId());
        log.info("  - Nom complet: {} {}", profile.getPrenom(), profile.getNom());
        log.info("  - Email: {}", profile.getEmail());
        log.info("  - Téléphone: {}", profile.getTelephone());
        log.info("  - Ville: {}", profile.getVille());
        log.info("  - Titre: {}", profile.getTitreProfessionnel());
        log.info("  - Disponibilité: {}", profile.getDisponibilite());
        log.info("  - Expérience: {} ans", profile.getExperienceAnnees());
        log.info("  - Diplôme: {}", profile.getNiveauEtude());
        log.info("  - Compétences: {}", profile.getCompetencesAsStrings());
        log.info("  - Candidature ID: {}", candidature.getId());
        log.info("==========================================");
        
        return candidature;
    }
}