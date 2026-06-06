// com.example.recrutement.interview.repository.InterviewRepository.java
package com.example.recrutement.interview.repository;

import com.example.recrutement.dashboard.service.DashboardService.ChartDataDTO;
import com.example.recrutement.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // Recherche par candidature
    List<Interview> findByCandidatureId(Long candidatureId);

    // Recherche par candidat (via candidature)
    @Query("SELECT i FROM Interview i WHERE i.candidature.candidate.id = :candidatId")
    List<Interview> findByCandidatId(@Param("candidatId") Long candidatId);

    // Recherche par offre (via candidature)
    @Query("SELECT i FROM Interview i WHERE i.candidature.offre.id = :offreId")
    List<Interview> findByOffreId(@Param("offreId") UUID offreId);

    // Entretiens à venir
    @Query("SELECT i FROM Interview i WHERE i.dateEntretien >= :now ORDER BY i.dateEntretien ASC")
    List<Interview> findUpcomingInterviews(@Param("now") LocalDateTime now);

    // Entretiens entre deux dates
    @Query("SELECT i FROM Interview i WHERE i.dateEntretien BETWEEN :start AND :end")
    List<Interview> findByDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Filtre par statut
    List<Interview> findByStatut(Interview.InterviewStatus statut);

    // Filtre par type
    List<Interview> findByType(Interview.InterviewType type);

    // Entretiens à venir d'un candidat
    @Query("SELECT i FROM Interview i WHERE i.candidature.candidate.id = :candidatId AND i.dateEntretien >= :now ORDER BY i.dateEntretien ASC")
    List<Interview> findUpcomingByCandidatId(@Param("candidatId") Long candidatId, @Param("now") LocalDateTime now);

    List<Interview> findTop5ByOrderByDateEntretienDesc();

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.dateEntretien BETWEEN :start AND :end")
    long countByDateEntretienBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);}