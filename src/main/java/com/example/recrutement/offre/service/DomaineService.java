// src/main/java/com/example/recrutement/offre/service/DomaineService.java
package com.example.recrutement.offre.service;

import com.example.recrutement.offre.dto.DomaineDTO;
import com.example.recrutement.offre.entity.Domaine;
import com.example.recrutement.offre.exception.ResourceNotFoundException;
import com.example.recrutement.offre.repository.DomaineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DomaineService {

    private final DomaineRepository domaineRepository;

    public List<DomaineDTO> getAllDomaines() {
        return domaineRepository.findAllByActifTrueOrderByOrdreAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DomaineDTO> getAllDomainesAdmin() {
        return domaineRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DomaineDTO getDomaineById(UUID id) {
        Domaine domaine = domaineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domaine non trouvé"));
        return convertToDTO(domaine);
    }

    @Transactional
    public DomaineDTO createDomaine(DomaineDTO dto) {
        if (domaineRepository.existsByNom(dto.getNom())) {
            throw new IllegalArgumentException("Un domaine avec ce nom existe déjà");
        }

        Domaine domaine = Domaine.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .couleur(dto.getCouleur())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .ordre(dto.getOrdre() != null ? dto.getOrdre() : 0)
                .build();

        Domaine saved = domaineRepository.save(domaine);
        return convertToDTO(saved);
    }

    @Transactional
    public DomaineDTO updateDomaine(UUID id, DomaineDTO dto) {
        Domaine domaine = domaineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domaine non trouvé"));

        if (!domaine.getNom().equals(dto.getNom()) && domaineRepository.existsByNom(dto.getNom())) {
            throw new IllegalArgumentException("Un domaine avec ce nom existe déjà");
        }

        domaine.setNom(dto.getNom());
        domaine.setDescription(dto.getDescription());
        domaine.setCouleur(dto.getCouleur());
        domaine.setActif(dto.getActif());
        domaine.setOrdre(dto.getOrdre());

        Domaine updated = domaineRepository.save(domaine);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteDomaine(UUID id) {
        Domaine domaine = domaineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domaine non trouvé"));
        domaineRepository.delete(domaine);
    }

    private DomaineDTO convertToDTO(Domaine domaine) {
        return DomaineDTO.builder()
                .id(domaine.getId())
                .nom(domaine.getNom())
                .description(domaine.getDescription())
                .couleur(domaine.getCouleur())
                .actif(domaine.getActif())
                .ordre(domaine.getOrdre())
                .createdAt(domaine.getCreatedAt())
                .updatedAt(domaine.getUpdatedAt())
                .build();
    }
}