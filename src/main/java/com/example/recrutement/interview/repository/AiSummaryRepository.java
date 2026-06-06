// com.example.recrutement.interview.repository/AiSummaryRepository.java
package com.example.recrutement.interview.repository;

import com.example.recrutement.interview.entity.AiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {
    Optional<AiSummary> findByCandidatureId(Long candidatureId);
    List<AiSummary> findByCandidatureIdOrderByCreatedAtDesc(Long candidatureId);
    @Query("SELECT s FROM AiSummary s WHERE s.candidature.id = :candidatureId")
    Optional<AiSummary> findByCandidatureIdJPQL(@Param("candidatureId") Long candidatureId);
    
}