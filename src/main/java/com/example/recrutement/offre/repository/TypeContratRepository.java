// src/main/java/com/example/recrutement/offre/repository/TypeContratRepository.java
package com.example.recrutement.offre.repository;

import com.example.recrutement.offre.entity.TypeContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeContratRepository extends JpaRepository<TypeContrat, UUID> {
    Optional<TypeContrat> findByNom(String nom);
    List<TypeContrat> findAllByActifTrueOrderByOrdreAsc();
    boolean existsByNom(String nom);
}