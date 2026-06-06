package com.example.recrutement.candidature.service;

import com.example.recrutement.candidature.dto.CandidatureResponseDTO;
import com.example.recrutement.candidature.dto.CreateCandidatureDTO;
import com.example.recrutement.candidature.entity.StatutCandidature;

import java.util.List;
import java.util.UUID;

public interface CandidatureService {
    
    List<CandidatureResponseDTO> getAllCandidatures();
    
    CandidatureResponseDTO postuler(CreateCandidatureDTO dto);
    
    List<CandidatureResponseDTO> getByOffre(UUID offreId);
    
    List<CandidatureResponseDTO> getByOffreAndStatut(UUID offreId, StatutCandidature statut);
    
    CandidatureResponseDTO updateStatus(Long candidatureId, StatutCandidature newStatus, Long rhId, String commentaire);
    
    CandidatureResponseDTO getCandidatureById(Long id);
    
    List<CandidatureResponseDTO> getByCandidate(Long candidateId);
    
    void deleteCandidature(Long id);
    
    CandidatureResponseDTO addNote(Long candidatureId, String contenu, Long rhId);
    
    // ✅ NOUVELLE MÉTHODE
    List<StatutCandidature> getAllowedNextStatuses(Long candidatureId);
}