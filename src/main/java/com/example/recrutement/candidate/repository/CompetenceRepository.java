package com.example.recrutement.candidate.repository;

import com.example.recrutement.candidate.entity.Competence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CompetenceRepository extends JpaRepository<Competence, Long> {
    
    List<Competence> findByCandidateProfileId(Long candidateProfileId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Competence c WHERE c.candidateProfile.id = :profileId")
    void deleteByCandidateProfileId(@Param("profileId") Long profileId);
    
    boolean existsByNomAndCandidateProfileId(String nom, Long candidateProfileId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Competence c WHERE c.candidateProfile.id = :profileId AND c.nom = :nom")
    void deleteByCandidateProfileIdAndNom(@Param("profileId") Long profileId, @Param("nom") String nom);
}