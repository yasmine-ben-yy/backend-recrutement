package com.example.recrutement.offre.repository;

import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.entity.OffreEmploi.OffreStatut;
import com.example.recrutement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OffreRepository extends JpaRepository<OffreEmploi, UUID> {

    // ✅ AJOUTÉ — manquait complètement
    @Query("SELECT DISTINCT o FROM OffreEmploi o " +
           "LEFT JOIN FETCH o.typeContrat " +
           "LEFT JOIN FETCH o.domaine " +
           "WHERE o.id = :id")
    Optional<OffreEmploi> findByIdWithRelations(@Param("id") UUID id);

    @Query("SELECT DISTINCT o FROM OffreEmploi o " +
           "LEFT JOIN FETCH o.typeContrat " +
           "LEFT JOIN FETCH o.domaine " +
           "WHERE o.statut = :statut " +
           "ORDER BY o.datePublication DESC")
    List<OffreEmploi> findByStatutOrderByDatePublicationDesc(@Param("statut") OffreEmploi.OffreStatut statut);

    @Query("SELECT DISTINCT o FROM OffreEmploi o " +
           "LEFT JOIN FETCH o.typeContrat " +
           "LEFT JOIN FETCH o.domaine " +
           "WHERE o.createdBy = :user " +
           "ORDER BY o.createdAt DESC")
    List<OffreEmploi> findByCreatedByOrderByCreatedAtDesc(@Param("user") User user);

    @Query("SELECT DISTINCT o FROM OffreEmploi o " +
           "LEFT JOIN FETCH o.typeContrat " +
           "LEFT JOIN FETCH o.domaine " +
           "WHERE o.createdBy = :user")
    Page<OffreEmploi> findByCreatedBy(@Param("user") User user, Pageable pageable);

    List<OffreEmploi> findByStatut(OffreEmploi.OffreStatut statut);

    @Query("SELECT DISTINCT o FROM OffreEmploi o " +
           "LEFT JOIN FETCH o.typeContrat " +
           "LEFT JOIN FETCH o.domaine " +
           "WHERE o.createdBy = :user " +
           "AND (LOWER(o.titre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<OffreEmploi> searchByUser(@Param("user") User user, @Param("search") String search);

    @Query("SELECT DISTINCT o FROM OffreEmploi o " +
           "LEFT JOIN FETCH o.typeContrat " +
           "LEFT JOIN FETCH o.domaine " +
           "WHERE o.id = :id AND o.createdBy = :user")
    Optional<OffreEmploi> findByIdAndCreatedBy(@Param("id") UUID id, @Param("user") User user);

    @Query("SELECT DISTINCT o FROM OffreEmploi o " +
           "LEFT JOIN FETCH o.typeContrat " +
           "LEFT JOIN FETCH o.domaine " +
           "WHERE o.createdBy = :user " +
           "ORDER BY o.createdAt DESC")
    List<OffreEmploi> findTop5ByCreatedByOrderByCreatedAtDesc(@Param("user") User user);

    @Query("SELECT o FROM OffreEmploi o WHERE o.statut = :statut AND o.dateCloture IS NOT NULL")
    List<OffreEmploi> findByStatutAndDateClotureNotNull(@Param("statut") OffreEmploi.OffreStatut statut);

    @Query("SELECT COUNT(o) FROM OffreEmploi o WHERE o.datePublication BETWEEN :start AND :end")
    long countByDatePublicationBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    // Pour la clôture automatique
    List<OffreEmploi> findByStatutAndDateClotureBefore(OffreEmploi.OffreStatut statut, LocalDateTime date);}