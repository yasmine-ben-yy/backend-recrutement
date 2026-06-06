package com.example.recrutement.interview.repository;

import com.example.recrutement.interview.entity.EvaluationCriteres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationCriteresRepository extends JpaRepository<EvaluationCriteres, Long> {
}