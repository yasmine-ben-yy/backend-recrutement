package com.example.recrutement.rh.repository;

import com.example.recrutement.rh.entity.RhProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RhProfileRepository extends JpaRepository<RhProfile, Long> {
    Optional<RhProfile> findByEmail(String email);
    boolean existsByEmail(String email);
}