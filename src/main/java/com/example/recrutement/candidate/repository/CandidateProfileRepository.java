// com.example.recrutement.candidate.repository.CandidateProfileRepository.java
package com.example.recrutement.candidate.repository;

import com.example.recrutement.candidate.entity.CandidateProfile;

import com.example.recrutement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository 
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    Optional<CandidateProfile> findByUser(User user);
    Optional<CandidateProfile> findByUserEmail(String email);
    boolean existsByUserEmail(String email);
}