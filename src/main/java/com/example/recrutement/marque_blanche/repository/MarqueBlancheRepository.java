// com.example.recrutement.marque_blanche.repository.MarqueBlancheRepository.java
package com.example.recrutement.marque_blanche.repository;

import com.example.recrutement.marque_blanche.entity.MarqueBlanche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MarqueBlancheRepository extends JpaRepository<MarqueBlanche, Long> {
    
    @Query("SELECT m FROM MarqueBlanche m ORDER BY m.id DESC LIMIT 1")
    Optional<MarqueBlanche> findLatest();
    
    @Query("SELECT COUNT(m) FROM MarqueBlanche m")
    long countConfigurations();
}