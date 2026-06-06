// src/main/java/com/example/recrutement/offre/service/TypeContratService.java
package com.example.recrutement.offre.service;

import com.example.recrutement.offre.dto.TypeContratDTO;
import com.example.recrutement.offre.entity.TypeContrat;
import com.example.recrutement.offre.exception.ResourceNotFoundException;
import com.example.recrutement.offre.repository.TypeContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeContratService {

    private final TypeContratRepository typeContratRepository;

    public List<TypeContratDTO> getAllTypesContrat() {
        return typeContratRepository.findAllByActifTrueOrderByOrdreAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TypeContratDTO> getAllTypesContratAdmin() {
        return typeContratRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TypeContratDTO getTypeContratById(UUID id) {
        TypeContrat typeContrat = typeContratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de contrat non trouvé"));
        return convertToDTO(typeContrat);
    }

    @Transactional
    public TypeContratDTO createTypeContrat(TypeContratDTO dto) {
        if (typeContratRepository.existsByNom(dto.getNom())) {
            throw new IllegalArgumentException("Un type de contrat avec ce nom existe déjà");
        }

        TypeContrat typeContrat = TypeContrat.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .couleur(dto.getCouleur())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .ordre(dto.getOrdre() != null ? dto.getOrdre() : 0)
                .build();

        TypeContrat saved = typeContratRepository.save(typeContrat);
        return convertToDTO(saved);
    }

    @Transactional
    public TypeContratDTO updateTypeContrat(UUID id, TypeContratDTO dto) {
        TypeContrat typeContrat = typeContratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de contrat non trouvé"));

        // Vérifier si le nom est déjà pris par un autre
        if (!typeContrat.getNom().equals(dto.getNom()) && typeContratRepository.existsByNom(dto.getNom())) {
            throw new IllegalArgumentException("Un type de contrat avec ce nom existe déjà");
        }

        typeContrat.setNom(dto.getNom());
        typeContrat.setDescription(dto.getDescription());
        typeContrat.setCouleur(dto.getCouleur());
        
        typeContrat.setActif(dto.getActif());
        typeContrat.setOrdre(dto.getOrdre());

        TypeContrat updated = typeContratRepository.save(typeContrat);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteTypeContrat(UUID id) {
        TypeContrat typeContrat = typeContratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de contrat non trouvé"));

        // Vérifier si le type est utilisé par des offres
        // Cette vérification nécessite une méthode dans le repository des offres
        typeContratRepository.delete(typeContrat);
    }

    private TypeContratDTO convertToDTO(TypeContrat typeContrat) {
        return TypeContratDTO.builder()
                .id(typeContrat.getId())
                .nom(typeContrat.getNom())
                .description(typeContrat.getDescription())
                .actif(typeContrat.getActif())
                .couleur(typeContrat.getCouleur())
                .ordre(typeContrat.getOrdre())
                .createdAt(typeContrat.getCreatedAt())
                .updatedAt(typeContrat.getUpdatedAt())
                .build();
    }
}