package com.example.recrutement.candidature.repository;

import com.example.recrutement.candidature.entity.CandidatureNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatureNoteRepository extends JpaRepository<CandidatureNote, Long> {}