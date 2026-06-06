package com.example.recrutement.candidature.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.candidate.service.AIMatchingService;
import com.example.recrutement.candidature.dto.CandidatSimplifieDTO;
import com.example.recrutement.candidature.dto.CandidatureResponseDTO;
import com.example.recrutement.candidature.dto.CreateCandidatureDTO;
import com.example.recrutement.candidature.dto.HistoriqueNoteDTO;
import com.example.recrutement.candidature.dto.HistoriqueStatutDTO;
import com.example.recrutement.candidature.dto.OffreSimplifieeDTO;
import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.entity.CandidatureNote;
import com.example.recrutement.candidature.entity.CandidatureStatusHistory;
import com.example.recrutement.candidature.entity.StatutCandidature;
import com.example.recrutement.candidature.exception.CandidateNotFoundException;
import com.example.recrutement.candidature.exception.CandidatureAlreadyExistsException;
import com.example.recrutement.candidature.exception.CandidatureNotFoundException;
import com.example.recrutement.candidature.exception.OffreNotFoundException;
import com.example.recrutement.candidature.exception.OffreNotOpenException;
import com.example.recrutement.candidature.exception.UserNotFoundException;
import com.example.recrutement.candidature.repository.CandidatureHistoryRepository;
import com.example.recrutement.candidature.repository.CandidatureNoteRepository;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.repository.OffreRepository;
import com.example.recrutement.rh.entity.RhProfile;
import com.example.recrutement.service.IEmailService;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidatureServiceImpl implements CandidatureService {

    private final CandidatureRepository repository;
    private final CandidateProfileRepository candidateRepository;
    private final OffreRepository offreRepository;
    private final UserRepository userRepository;
    private final CandidatureHistoryRepository historyRepository;
    private final CandidatureNoteRepository noteRepository;
    private final AIMatchingService aiMatchingService;
    private final IEmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureResponseDTO> getAllCandidatures() {
        log.info("Récupération de toutes les candidatures");
        return repository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CandidatureResponseDTO postuler(CreateCandidatureDTO dto) {
        log.info("Tentative de candidature - Candidate: {}, Offre: {}", 
                 dto.getCandidateId(), dto.getOffreId());

        if (repository.existsByCandidateIdAndOffreId(dto.getCandidateId(), dto.getOffreId())) {
            throw new CandidatureAlreadyExistsException(dto.getCandidateId(), dto.getOffreId());
        }

        var candidate = candidateRepository.findById(dto.getCandidateId())
                .orElseThrow(() -> new CandidateNotFoundException("Candidat non trouvé avec l'ID: " + dto.getCandidateId()));

        var offre = offreRepository.findById(dto.getOffreId())
                .orElseThrow(() -> new OffreNotFoundException("Offre non trouvée avec l'ID: " + dto.getOffreId()));

        if (!offre.peutPostuler()) {
            throw new OffreNotOpenException("Cette offre n'est pas ouverte aux candidatures");
        }

        double matchingScore = aiMatchingService.calculateMatchingScore(candidate, offre);
        log.info("🤖 Score IA calculé pour la candidature: {}%", Math.round(matchingScore));

        Candidature candidature = Candidature.builder()
                .candidate(candidate)
                .offre(offre)
                .statut(StatutCandidature.EN_ATTENTE)
                .dateCandidature(LocalDateTime.now())
                .cvSnapshotPath(candidate.getCvPrincipalPath())
                .lettreMotivationPath(dto.getLettreMotivationPath())
                .matchingScore(matchingScore)
                .build();

        candidate.addCandidature(candidature);
        offre.addCandidature(candidature);

        Candidature savedCandidature = repository.save(candidature);
        log.info("Candidature créée avec succès - ID: {}, Score IA: {}%", 
                 savedCandidature.getId(), Math.round(matchingScore));

        return convertToDTO(savedCandidature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureResponseDTO> getByOffre(UUID offreId) {
        log.info("Récupération des candidatures pour l'offre: {}", offreId);
        
        if (!offreRepository.existsById(offreId)) {
            throw new OffreNotFoundException("Offre non trouvée avec l'ID: " + offreId);
        }

        return repository.findByOffreId(offreId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureResponseDTO> getByOffreAndStatut(UUID offreId, StatutCandidature statut) {
        log.info("Récupération des candidatures pour l'offre: {} avec statut: {}", offreId, statut);
        
        if (!offreRepository.existsById(offreId)) {
            throw new OffreNotFoundException("Offre non trouvée avec l'ID: " + offreId);
        }
        
        return repository.findByOffreIdAndStatut(offreId, statut)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CandidatureResponseDTO updateStatus(Long candidatureId, StatutCandidature newStatus, Long rhId, String commentaire) {
        log.info("Mise à jour du statut - Candidature: {}, Nouveau statut: {}, RH: {}", 
                 candidatureId, newStatus, rhId);

        Candidature candidature = repository.findById(candidatureId)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée avec l'ID: " + candidatureId));

        var rh = userRepository.findById(rhId)
                .orElseThrow(() -> new UserNotFoundException("RH non trouvé avec l'ID: " + rhId));

        StatutCandidature oldStatus = candidature.getStatut();

        if (oldStatus == newStatus) {
            log.info("Le statut est déjà {}, aucun changement effectué", newStatus);
            return convertToDTO(candidature);
        }
        
        if (!candidature.canTransitionTo(newStatus)) {
            log.error("🚫 Transition non autorisée: {} → {}", oldStatus, newStatus);
            throw new IllegalStateException(
                String.format("Transition impossible : de '%s' vers '%s' n'est pas autorisée dans le pipeline RH", 
                              oldStatus, newStatus)
            );
        }

        candidature.setStatut(newStatus);

        CandidatureStatusHistory history = CandidatureStatusHistory.builder()
                .candidature(candidature)
                .ancienStatut(oldStatus)
                .nouveauStatut(newStatus)
                .changedBy(rh)
                .changeDate(LocalDateTime.now())
                .commentaire(commentaire)
                .build();

        historyRepository.save(history);
        candidature.addHistoriqueStatut(history);

        Candidature updatedCandidature = repository.save(candidature);
        log.info("Statut mis à jour avec succès - Candidature: {}, Ancien: {}, Nouveau: {}", 
                 candidatureId, oldStatus, newStatus);

        envoyerEmailChangementStatut(updatedCandidature, oldStatus.toString(), newStatus.toString(), commentaire);

        return convertToDTO(updatedCandidature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatutCandidature> getAllowedNextStatuses(Long candidatureId) {
        Candidature candidature = repository.findById(candidatureId)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée avec l'ID: " + candidatureId));
        
        return candidature.getAllowedNextStatuses();
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatureResponseDTO getCandidatureById(Long id) {
        log.info("Récupération de la candidature: {}", id);
        
        Candidature candidature = repository.findById(id)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée avec l'ID: " + id));
        
        return convertToDTO(candidature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureResponseDTO> getByCandidate(Long candidateId) {
        log.info("Récupération des candidatures du candidat: {}", candidateId);
        
        if (!candidateRepository.existsById(candidateId)) {
            throw new CandidateNotFoundException("Candidat non trouvé avec l'ID: " + candidateId);
        }
        
        return repository.findByCandidateId(candidateId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCandidature(Long id) {
        log.info("Suppression de la candidature: {}", id);
        
        Candidature candidature = repository.findById(id)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée avec l'ID: " + id));
        
        repository.delete(candidature);
        log.info("Candidature supprimée avec succès: {}", id);
    }

    @Override
    @Transactional
    public CandidatureResponseDTO addNote(Long candidatureId, String contenu, Long rhId) {
        log.info("Ajout d'une note à la candidature: {} par RH: {}", candidatureId, rhId);
        
        Candidature candidature = repository.findById(candidatureId)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée avec l'ID: " + candidatureId));
        
        var rh = userRepository.findById(rhId)
                .orElseThrow(() -> new UserNotFoundException("RH non trouvé avec l'ID: " + rhId));
        
        CandidatureNote note = CandidatureNote.builder()
                .candidature(candidature)
                .rh(rh)
                .contenu(contenu)
                .createdAt(LocalDateTime.now())
                .build();
        
        noteRepository.save(note);
        candidature.addNote(note);
        
        return convertToDTO(candidature);
    }

    private void envoyerEmailChangementStatut(Candidature candidature, String ancienStatut, String nouveauStatut, String commentaire) {
        log.info("📧 ENVOI EMAIL - Statut: {} -> {}, Candidature: {}", ancienStatut, nouveauStatut, candidature.getId());
        
        new Thread(() -> {
            try {
                Thread.sleep(500);
                emailService.sendStatusChangeEmail(candidature, ancienStatut, nouveauStatut, commentaire);
                log.info("✅ Email de changement de statut envoyé pour la candidature: {}", candidature.getId());
            } catch (Exception e) {
                log.error("❌ Erreur lors de l'envoi de l'email: {}", e.getMessage());
            }
        }).start();
    }

    private CandidatureResponseDTO convertToDTO(Candidature candidature) {
        if (candidature == null) return null;

        CandidatSimplifieDTO candidatDTO = convertToCandidatSimplifieDTO(candidature.getCandidate());
        OffreSimplifieeDTO offreDTO = convertToOffreSimplifieeDTO(candidature.getOffre());

        List<HistoriqueStatutDTO> historiqueStatutsDTO = candidature.getHistoriqueStatuts()
                .stream()
                .map(this::convertToHistoriqueStatutDTO)
                .collect(Collectors.toList());

        List<HistoriqueNoteDTO> historiqueNotesDTO = candidature.getNotes()
                .stream()
                .map(this::convertToHistoriqueNoteDTO)
                .collect(Collectors.toList());

        return CandidatureResponseDTO.builder()
                .id(candidature.getId())
                .dateCandidature(candidature.getDateCandidature())
                .statut(candidature.getStatut())
                .matchingScore(candidature.getMatchingScore())
                .cvSnapshotPath(candidature.getCvSnapshotPath())
                .lettreMotivationPath(candidature.getLettreMotivationPath())
                // ✅ AJOUTER ces deux lignes
                .candidatId(candidature.getCandidate() != null ? candidature.getCandidate().getId() : null)
                .offreId(candidature.getOffre() != null ? candidature.getOffre().getId().toString() : null)
                .candidate(candidatDTO)
                .offre(offreDTO)
                .historiqueStatuts(historiqueStatutsDTO)
                .historiqueNotes(historiqueNotesDTO)
                .build();
    }

    private CandidatSimplifieDTO convertToCandidatSimplifieDTO(CandidateProfile candidate) {
        if (candidate == null) return null;
        
        return CandidatSimplifieDTO.builder()
                .id(candidate.getId())
                .nom(candidate.getNom())
                .prenom(candidate.getPrenom())
                .email(candidate.getUser() != null ? candidate.getUser().getEmail() : null)
                .telephone(candidate.getTelephone())
                .titreProfessionnel(candidate.getTitreProfessionnel())
                .experienceAnnees(candidate.getExperienceAnnees())
                .niveauEtude(candidate.getNiveauEtude())
                .ville(candidate.getVille())
                .dateNaissance(candidate.getDateNaissance() != null ? candidate.getDateNaissance().toString() : null)
                .pays(candidate.getPays())
                .cvPrincipalPath(candidate.getCvPrincipalPath())
                .build();
    }

    private OffreSimplifieeDTO convertToOffreSimplifieeDTO(OffreEmploi offre) {
        if (offre == null) return null;
        
        return OffreSimplifieeDTO.builder()
                .id(offre.getId())
                .titre(offre.getTitre() != null ? offre.getTitre() : "Offre inconnue")
                .description(truncate(offre.getDescription(), 200))
                .competencesRequises(offre.getCompetencesRequises())
                .typeContrat(offre.getTypeContrat() != null ? offre.getTypeContrat().getNom() : "Non spécifié")
                .domaine(offre.getDomaine() != null ? offre.getDomaine().getNom() : "Non spécifié")
                .localisation(offre.getLocalisation() != null ? offre.getLocalisation() : "Non spécifiée")
                .fourchetteSalaire(offre.getFourchetteSalaire())
                .teletravailPossible(offre.getTeletravailPossible())
                .niveauEtude(offre.getNiveauEtude() != null ? offre.getNiveauEtude() : "Non spécifié")
                .datePublication(offre.getDatePublication())
                .dateCloture(offre.getDateCloture())
                .statut(offre.getStatut() != null ? offre.getStatut().toString() : null)
                .nombrePostesRestants(offre.getNombrePostes())
                .peutPostuler(offre.peutPostuler())
                .build();
    }

    private String getRhName(User user) {
        if (user == null) return null;
        
        if (user.getRhProfile() != null) {
            RhProfile rh = user.getRhProfile();
            String prenom = rh.getPrenom() != null ? rh.getPrenom() : "";
            String nom = rh.getNom() != null ? rh.getNom() : "";
            String fullName = (prenom + " " + nom).trim();
            if (!fullName.isEmpty()) return fullName;
        }
        
        return user.getEmail();
    }

    private HistoriqueStatutDTO convertToHistoriqueStatutDTO(CandidatureStatusHistory history) {
        if (history == null) return null;
        
        String auteur = getRhName(history.getChangedBy());
        if (auteur == null) auteur = "Système";
        
        return HistoriqueStatutDTO.builder()
                .id(history.getId())
                .date(history.getChangeDate())
                .auteur(auteur)
                .ancienStatut(history.getAncienStatut() != null ? history.getAncienStatut().toString() : null)
                .nouveauStatut(history.getNouveauStatut() != null ? history.getNouveauStatut().toString() : null)
                .commentaire(history.getCommentaire())
                .build();
    }

    private HistoriqueNoteDTO convertToHistoriqueNoteDTO(CandidatureNote note) {
        if (note == null) return null;
        
        String auteur = getRhName(note.getRh());
        if (auteur == null) auteur = "Système";
        
        return HistoriqueNoteDTO.builder()
                .id(note.getId())
                .date(note.getCreatedAt())
                .auteur(auteur)
                .contenu(note.getContenu())
                .build();
    }

    private String truncate(String str, int length) {
        if (str == null || str.length() <= length) return str;
        return str.substring(0, length) + "...";
    }
}