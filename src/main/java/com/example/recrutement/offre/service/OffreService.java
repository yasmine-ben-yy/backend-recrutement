package com.example.recrutement.offre.service;

import com.example.recrutement.offre.dto.OffreRequest;
import com.example.recrutement.offre.dto.OffreResponse;
import com.example.recrutement.offre.dto.OffreSimpleResponse;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.entity.TypeContrat;
import com.example.recrutement.offre.entity.Domaine;
import com.example.recrutement.offre.exception.ResourceNotFoundException;
import com.example.recrutement.offre.repository.OffreRepository;
import com.example.recrutement.offre.repository.TypeContratRepository;
import com.example.recrutement.offre.repository.DomaineRepository;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import com.example.recrutement.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OffreService {

    private static final Logger log = LoggerFactory.getLogger(OffreService.class);

    private final OffreRepository offreRepository;
    private final UserRepository userRepository;
    private final TypeContratRepository typeContratRepository;
    private final DomaineRepository domaineRepository;
    private final NotificationService notificationService;

    // ================= PUBLIC =================

    @Transactional(readOnly = true)
    public List<OffreSimpleResponse> getOffresPubliees() {
        log.debug("Récupération des offres publiées");
        return offreRepository.findByStatutOrderByDatePublicationDesc(OffreEmploi.OffreStatut.PUBLIEE)
                .stream()
                .filter(offre -> !offre.estExpiree())
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OffreResponse getOffrePubliqueById(UUID id) {
        log.debug("Récupération de l'offre publique: {}", id);
        OffreEmploi offre = offreRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée"));

        if (!offre.estPubliee() || offre.estExpiree()) {
            throw new ResourceNotFoundException("Offre non disponible");
        }

        return convertToResponse(offre);
    }
    
    @Transactional(readOnly = true)
    public List<OffreSimpleResponse> getOffresAvecFiltres(
            String motCle,
            String localisation,
            String typeContrat,
            String domaine
    ) {
        log.debug("Recherche offres avec filtres: {}, {}, {}, {}", motCle, localisation, typeContrat, domaine);

        return offreRepository.findByStatutOrderByDatePublicationDesc(OffreEmploi.OffreStatut.PUBLIEE)
                .stream()
                .filter(offre -> !offre.estExpiree())
                .filter(o -> motCle == null || motCle.isEmpty() ||
                        o.getTitre().toLowerCase().contains(motCle.toLowerCase()) ||
                        (o.getDescription() != null && o.getDescription().toLowerCase().contains(motCle.toLowerCase())))
                .filter(o -> localisation == null || localisation.isEmpty() ||
                        (o.getLocalisation() != null && o.getLocalisation().toLowerCase().contains(localisation.toLowerCase())))
                .filter(o -> typeContrat == null || typeContrat.isEmpty() ||
                        (o.getTypeContrat() != null && o.getTypeContrat().getNom().equalsIgnoreCase(typeContrat)))
                .filter(o -> domaine == null || domaine.isEmpty() ||
                        (o.getDomaine() != null && o.getDomaine().getNom().equalsIgnoreCase(domaine)))
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
    }

    // ================= NOTIFICATIONS D'EXPIRATION =================

    @Transactional
    public void checkExpiringOffres() {
        log.info("🔍 Vérification des offres qui expirent bientôt...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in3Days = now.plusDays(3);
        LocalDateTime in7Days = now.plusDays(7);
        
        List<OffreEmploi> offres = offreRepository.findByStatutAndDateClotureNotNull(OffreEmploi.OffreStatut.PUBLIEE);
        
        for (OffreEmploi offre : offres) {
            if (offre.getDateCloture() == null) continue;
            
            LocalDateTime dateCloture = offre.getDateCloture();
            User rh = offre.getCreatedBy();
            
            if (rh == null) continue;
            
            if (dateCloture.isAfter(now) && dateCloture.isBefore(in7Days)) {
                notificationService.notifyOffreExpiringSoon(
                    rh.getId(),
                    offre.getTitre(),
                    dateCloture,
                    offre.getId(),
                    7
                );
            }
            else if (dateCloture.isAfter(now) && dateCloture.isBefore(in3Days)) {
                notificationService.notifyOffreExpiringSoon(
                    rh.getId(),
                    offre.getTitre(),
                    dateCloture,
                    offre.getId(),
                    3
                );
            }
            else if (dateCloture.toLocalDate().equals(now.toLocalDate())) {
                notificationService.notifyOffreExpiringToday(
                    rh.getId(),
                    offre.getTitre(),
                    dateCloture,
                    offre.getId()
                );
            }
        }
    }

    // ================= CLÔTURE AUTOMATIQUE =================

    /**
     * ✅ Clôture automatique des offres dont la date limite est dépassée
     */
    @Transactional
    public void cloturerOffresExpirees() {
        log.info("🔍 Vérification des offres à clôturer automatiquement...");
        
        LocalDateTime now = LocalDateTime.now();
        
        List<OffreEmploi> offresExpirees = offreRepository.findByStatutAndDateClotureBefore(
            OffreEmploi.OffreStatut.PUBLIEE, now);
        
        int countCloturees = 0;
        
        for (OffreEmploi offre : offresExpirees) {
            offre.setStatut(OffreEmploi.OffreStatut.CLOTUREE);
            offreRepository.save(offre);
            countCloturees++;
            log.info("🔒 Offre automatiquement clôturée: {} (ID: {}) - Date limite: {}", 
                offre.getTitre(), offre.getId(), offre.getDateCloture());
            
            User rh = offre.getCreatedBy();
            if (rh != null) {
                notificationService.notifyOffreExpired(rh.getId(), offre.getTitre(), offre.getId());
            }
        }
        
        log.info("✅ {} offre(s) clôturée(s) automatiquement", countCloturees);
    }

    // ================= RH - TOUTES LES OFFRES =================

    @Transactional
    public OffreResponse createOffre(OffreRequest request, String userEmail) {
        log.info("Création d'une offre par: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        TypeContrat typeContrat = typeContratRepository.findById(request.getTypeContratId())
                .orElseThrow(() -> new ResourceNotFoundException("Type de contrat non trouvé"));

        Domaine domaine = domaineRepository.findById(request.getDomaineId())
                .orElseThrow(() -> new ResourceNotFoundException("Domaine non trouvé"));

        OffreEmploi offre = new OffreEmploi();
        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setCompetencesRequises(request.getCompetencesRequises() != null
                ? request.getCompetencesRequises() : new ArrayList<>());
        offre.setNiveauEtude(request.getNiveauEtude());
        offre.setExperienceRequise(request.getExperienceRequise() != null ? request.getExperienceRequise() : 0);
        offre.setTypeContrat(typeContrat);
        offre.setDomaine(domaine);
        offre.setLocalisation(request.getLocalisation());
        offre.setSalaire(request.getSalaire());
        offre.setFourchetteSalaire(request.getFourchetteSalaire());
        offre.setTeletravailPossible(request.getTeletravailPossible() != null ? request.getTeletravailPossible() : false);
        offre.setDateCloture(request.getDateCloture());
        offre.setNombrePostes(request.getNombrePostes() != null ? request.getNombrePostes() : 1);
        offre.setCreatedBy(user);
        offre.setStatut(OffreEmploi.OffreStatut.BROUILLON);

        OffreEmploi savedOffre = offreRepository.save(offre);
        log.info("Offre créée: {} (ID: {}) par {}", savedOffre.getTitre(), savedOffre.getId(), user.getEmail());

        return convertToResponse(savedOffre);
    }

    @Transactional(readOnly = true)
    public List<OffreResponse> getOffresByUser(String userEmail) {
        log.debug("📋 Récupération de TOUTES les offres pour: {}", userEmail);
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        List<OffreEmploi> allOffres = offreRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        log.info("✅ {} offres trouvées au total", allOffres.size());

        return allOffres.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OffreResponse> getOffresByUser(String userEmail, Pageable pageable) {
        log.debug("📋 Récupération paginée de TOUTES les offres pour: {}", userEmail);
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Page<OffreEmploi> allOffres = offreRepository.findAll(pageable);
        return allOffres.map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public OffreResponse getOffreByIdAndUser(UUID id, String userEmail) {
        log.debug("Récupération de l'offre {} pour: {}", id, userEmail);
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        OffreEmploi offre = offreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée"));

        return convertToResponse(offre);
    }

    @Transactional(readOnly = true)
    public List<OffreResponse> searchOffres(String userEmail, String q) {
        log.debug("Recherche d'offres pour {} avec: {}", userEmail, q);
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        List<OffreEmploi> allOffres = offreRepository.findAll();
        List<OffreEmploi> results = allOffres.stream()
                .filter(o -> o.getTitre().toLowerCase().contains(q.toLowerCase()) ||
                           (o.getDescription() != null && o.getDescription().toLowerCase().contains(q.toLowerCase())) ||
                           (o.getLocalisation() != null && o.getLocalisation().toLowerCase().contains(q.toLowerCase())))
                .collect(Collectors.toList());

        return results.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional
    public OffreResponse updateOffre(UUID id, OffreRequest request, String userEmail) {
        log.info("Mise à jour de l'offre {} par: {}", id, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        OffreEmploi offre = offreRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée ou non autorisée"));

        if (offre.estPubliee()) {
            throw new IllegalStateException("Impossible de modifier une offre déjà publiée");
        }

        TypeContrat typeContrat = typeContratRepository.findById(request.getTypeContratId())
                .orElseThrow(() -> new ResourceNotFoundException("Type de contrat non trouvé"));

        Domaine domaine = domaineRepository.findById(request.getDomaineId())
                .orElseThrow(() -> new ResourceNotFoundException("Domaine non trouvé"));

        offre.setTitre(request.getTitre());
        offre.setDescription(request.getDescription());
        offre.setCompetencesRequises(request.getCompetencesRequises() != null ? request.getCompetencesRequises() : new ArrayList<>());
        offre.setNiveauEtude(request.getNiveauEtude());
        offre.setExperienceRequise(request.getExperienceRequise() != null ? request.getExperienceRequise() : 0);
        offre.setTypeContrat(typeContrat);
        offre.setDomaine(domaine);
        offre.setLocalisation(request.getLocalisation());
        offre.setSalaire(request.getSalaire());
        offre.setFourchetteSalaire(request.getFourchetteSalaire());
        offre.setTeletravailPossible(request.getTeletravailPossible() != null ? request.getTeletravailPossible() : false);
        offre.setDateCloture(request.getDateCloture());
        offre.setNombrePostes(request.getNombrePostes() != null ? request.getNombrePostes() : 1);

        OffreEmploi updatedOffre = offreRepository.save(offre);
        return convertToResponse(updatedOffre);
    }

    @Transactional
    public void publierOffre(UUID id, String userEmail) {
        changeStatutOffre(id, userEmail, OffreEmploi.OffreStatut.PUBLIEE);
    }

    @Transactional
    public void archiverOffre(UUID id, String userEmail) {
        changeStatutOffre(id, userEmail, OffreEmploi.OffreStatut.ARCHIVEE);
    }

    @Transactional
    public void cloturerOffre(UUID id, String userEmail) {
        changeStatutOffre(id, userEmail, OffreEmploi.OffreStatut.CLOTUREE);
    }

    @Transactional
    public OffreResponse duplicateOffre(UUID id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        OffreEmploi offre = offreRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée ou non autorisée"));

        OffreEmploi offreDupliquee = new OffreEmploi();
        offreDupliquee.setTitre(offre.getTitre() + " (Copie)");
        offreDupliquee.setDescription(offre.getDescription());
        offreDupliquee.setCompetencesRequises(new ArrayList<>(offre.getCompetencesRequises()));
        offreDupliquee.setNiveauEtude(offre.getNiveauEtude());
        offreDupliquee.setExperienceRequise(offre.getExperienceRequise());
        offreDupliquee.setTypeContrat(offre.getTypeContrat());
        offreDupliquee.setDomaine(offre.getDomaine());
        offreDupliquee.setLocalisation(offre.getLocalisation());
        offreDupliquee.setSalaire(offre.getSalaire());
        offreDupliquee.setFourchetteSalaire(offre.getFourchetteSalaire());
        offreDupliquee.setTeletravailPossible(offre.getTeletravailPossible());
        offreDupliquee.setDateCloture(offre.getDateCloture());
        offreDupliquee.setNombrePostes(offre.getNombrePostes());
        offreDupliquee.setCreatedBy(user);
        offreDupliquee.setStatut(OffreEmploi.OffreStatut.BROUILLON);

        OffreEmploi savedOffre = offreRepository.save(offreDupliquee);
        return convertToResponse(savedOffre);
    }

    @Transactional
    public void deleteOffre(UUID id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        OffreEmploi offre = offreRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée ou non autorisée"));

        if (offre.getCandidatures() != null && !offre.getCandidatures().isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une offre avec des candidatures");
        }

        offreRepository.delete(offre);
    }

    // ================= ADMIN =================

    @Transactional
    public OffreResponse publierOffreByAdmin(UUID id) {
        OffreEmploi offre = offreRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée"));
        offre.setStatut(OffreEmploi.OffreStatut.PUBLIEE);
        offre.setDatePublication(LocalDateTime.now());
        return convertToResponse(offreRepository.save(offre));
    }

    @Transactional
    public OffreResponse archiverOffreByAdmin(UUID id) {
        OffreEmploi offre = offreRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée"));
        offre.setStatut(OffreEmploi.OffreStatut.ARCHIVEE);
        return convertToResponse(offreRepository.save(offre));
    }

    // ================= HELPERS =================

    private void changeStatutOffre(UUID id, String userEmail, OffreEmploi.OffreStatut statut) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        OffreEmploi offre = offreRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée ou non autorisée"));

        offre.setStatut(statut);
        if (statut == OffreEmploi.OffreStatut.PUBLIEE && offre.getDatePublication() == null) {
            offre.setDatePublication(LocalDateTime.now());
        }
        offreRepository.save(offre);
    }

    private OffreResponse convertToResponse(OffreEmploi offre) {
        return OffreResponse.builder()
                .id(offre.getId())
                .titre(offre.getTitre())
                .description(offre.getDescription())
                .competencesRequises(offre.getCompetencesRequises())
                .niveauEtude(offre.getNiveauEtude())
                .experienceRequise(offre.getExperienceRequise())
                .typeContrat(offre.getTypeContrat() != null ? offre.getTypeContrat().getNom() : null)
                .typeContratId(offre.getTypeContrat() != null ? offre.getTypeContrat().getId() : null)
                .domaine(offre.getDomaine() != null ? offre.getDomaine().getNom() : null)
                .domaineId(offre.getDomaine() != null ? offre.getDomaine().getId() : null)
                .localisation(offre.getLocalisation())
                .salaire(offre.getSalaire())
                .fourchetteSalaire(offre.getFourchetteSalaire())
                .teletravailPossible(offre.getTeletravailPossible())
                .datePublication(offre.getDatePublication())
                .dateCloture(offre.getDateCloture())
                .statut(offre.getStatut() != null ? offre.getStatut().name() : null)
                .nombrePostes(offre.getNombrePostes())
                .peutPostuler(offre.peutPostuler())
                .estExpiree(offre.estExpiree())
                .createdAt(offre.getCreatedAt())
                .createdBy(offre.getCreatedBy() != null ? offre.getCreatedBy().getId() : null)
                .build();
    }

    private OffreSimpleResponse convertToSimpleResponse(OffreEmploi offre) {
        String description = offre.getDescription();
        if (description != null && description.length() > 200) {
            description = description.substring(0, 200) + "...";
        }

        List<String> competences = offre.getCompetencesRequises();
        if (competences != null && competences.size() > 3) {
            competences = competences.subList(0, 3);
        }

        return OffreSimpleResponse.builder()
                .id(offre.getId())
                .titre(offre.getTitre())
                .description(description)
                .competencesRequises(competences != null ? competences : new ArrayList<>())
                .typeContrat(offre.getTypeContrat() != null ? offre.getTypeContrat().getNom() : null)
                .domaine(offre.getDomaine() != null ? offre.getDomaine().getNom() : null)
                .localisation(offre.getLocalisation())
                .fourchetteSalaire(offre.getFourchetteSalaire())
                .teletravailPossible(offre.getTeletravailPossible())
                .datePublication(offre.getDatePublication())
                .dateCloture(offre.getDateCloture())
                .peutPostuler(offre.peutPostuler())
                .experienceRequise(offre.getExperienceRequise())
                .build();
    }
}