package com.example.recrutement.candidature.repository;

import com.example.recrutement.candidature.entity.CandidatureStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatureHistoryRepository extends JpaRepository<CandidatureStatusHistory, Long> {}