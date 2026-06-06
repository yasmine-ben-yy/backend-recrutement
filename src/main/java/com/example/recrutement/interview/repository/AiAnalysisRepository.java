package com.example.recrutement.interview.repository;

import com.example.recrutement.interview.entity.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    
    // ✅ Cette méthode existe déjà
    Optional<AiAnalysis> findByCandidatureIdAndOffreId(Long candidatureId, UUID offreId);
    
    // ✅ AJOUTER CETTE MÉTHODE
    Optional<AiAnalysis> findByCandidatureId(Long candidatureId);
    
    List<AiAnalysis> findByCandidatureIdOrderByCreatedAtDesc(Long candidatureId);
    
    List<AiAnalysis> findByOffreIdOrderByCreatedAtDesc(UUID offreId);
}