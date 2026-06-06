package com.example.recrutement.candidature.repository;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.entity.StatutCandidature;
import com.example.recrutement.dashboard.service.DashboardService.ChartDataDTO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    List<Candidature> findByOffreId(UUID offreId);
    
    List<Candidature> findByCandidateId(Long candidateId);
    
    List<Candidature> findByOffreIdAndStatut(UUID offreId, StatutCandidature statut);
    
    boolean existsByCandidateIdAndOffreId(Long candidateId, UUID offreId);
    
    // ✅ MODIFIER : Remplacer countByPipelineStage par countByStatut
    @Query("SELECT c.statut, COUNT(c) FROM Candidature c GROUP BY c.statut")
    List<Object[]> countByStatut();
    
    // Optionnel : Compter par statut pour une offre spécifique
    @Query("SELECT c.statut, COUNT(c) FROM Candidature c WHERE c.offre.id = :offreId GROUP BY c.statut")
    List<Object[]> countByStatutAndOffre(@Param("offreId") UUID offreId);
    
    // Compter par statut pour un candidat spécifique
    @Query("SELECT c.statut, COUNT(c) FROM Candidature c WHERE c.candidate.id = :candidateId GROUP BY c.statut")
    List<Object[]> countByStatutAndCandidate(@Param("candidateId") Long candidateId);

    List<Candidature> findTop5ByOrderByDateCandidatureDesc();

    @Query("SELECT COUNT(c) FROM Candidature c WHERE c.dateCandidature BETWEEN :start AND :end")
    long countByDateCandidatureBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);}