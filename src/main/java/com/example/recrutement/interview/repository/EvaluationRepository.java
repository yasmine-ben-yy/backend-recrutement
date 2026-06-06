package com.example.recrutement.interview.repository;

import com.example.recrutement.interview.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByCandidatId(Long candidatId);
    List<Evaluation> findByOffreId(UUID offreId);
    List<Evaluation> findByInterviewId(Long interviewId);
}