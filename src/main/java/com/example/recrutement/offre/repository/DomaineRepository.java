// src/main/java/com/example/recrutement/offre/repository/DomaineRepository.java
package com.example.recrutement.offre.repository;

import com.example.recrutement.offre.entity.Domaine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DomaineRepository extends JpaRepository<Domaine, UUID> {
    Optional<Domaine> findByNom(String nom);
    List<Domaine> findAllByActifTrueOrderByOrdreAsc();
    boolean existsByNom(String nom);
}