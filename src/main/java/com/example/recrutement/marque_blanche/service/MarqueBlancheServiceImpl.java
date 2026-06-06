// src/main/java/com/example/recrutement/marque_blanche/service/MarqueBlancheServiceImpl.java
package com.example.recrutement.marque_blanche.service;

import com.example.recrutement.marque_blanche.dto.MarqueBlancheDTO;
import com.example.recrutement.marque_blanche.entity.MarqueBlanche;
import com.example.recrutement.marque_blanche.repository.MarqueBlancheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarqueBlancheServiceImpl implements MarqueBlancheService {

    private final MarqueBlancheRepository repository;

    @Override
    public MarqueBlancheDTO getActiveConfiguration() {
        log.info("📥 Récupération de la configuration active");
        
        return repository.findLatest()
                .map(this::convertToDTO)
                .orElseGet(() -> {
                    log.info("⚠️ Aucune configuration trouvée, création par défaut");
                    return initializeDefaultConfiguration();
                });
    }

    @Override
    public MarqueBlancheDTO updateConfiguration(MarqueBlancheDTO dto) {
        log.info("📝 Mise à jour de la configuration");
        
        MarqueBlanche config = repository.findLatest()
                .orElseGet(MarqueBlanche::new);
        
        if (dto.getNomApplication() != null) config.setNomApplication(dto.getNomApplication());
        if (dto.getLogoEntreprise() != null) config.setLogoEntreprise(dto.getLogoEntreprise());
        if (dto.getCouleurPrimaire() != null) config.setCouleurPrimaire(dto.getCouleurPrimaire());
        if (dto.getCouleurSecondaire() != null) config.setCouleurSecondaire(dto.getCouleurSecondaire());
        if (dto.getCouleurAccent() != null) config.setCouleurAccent(dto.getCouleurAccent());
        if (dto.getCouleurFond() != null) config.setCouleurFond(dto.getCouleurFond());
        if (dto.getEmailContact() != null) config.setEmailContact(dto.getEmailContact());
        if (dto.getTelephoneContact() != null) config.setTelephoneContact(dto.getTelephoneContact());
        if (dto.getAdresseEntreprise() != null) config.setAdresseEntreprise(dto.getAdresseEntreprise());
        if (dto.getSiteWeb() != null) config.setSiteWeb(dto.getSiteWeb());
        if (dto.getTexteMentionsLegales() != null) config.setTexteMentionsLegales(dto.getTexteMentionsLegales());
        if (dto.getTextePolitiqueConfidentialite() != null) config.setTextePolitiqueConfidentialite(dto.getTextePolitiqueConfidentialite());
        if (dto.getAfficherChampTelephone() != null) config.setAfficherChampTelephone(dto.getAfficherChampTelephone());
        if (dto.getAfficherChampLinkedin() != null) config.setAfficherChampLinkedin(dto.getAfficherChampLinkedin());
        if (dto.getAfficherChampPortfolio() != null) config.setAfficherChampPortfolio(dto.getAfficherChampPortfolio());
        if (dto.getAfficherChampCourriel() != null) config.setAfficherChampCourriel(dto.getAfficherChampCourriel());
        if (dto.getStatutInitialCandidature() != null) config.setStatutInitialCandidature(dto.getStatutInitialCandidature());
        
        MarqueBlanche saved = repository.save(config);
        log.info("✅ Configuration mise à jour - ID: {}", saved.getId());
        
        return convertToDTO(saved);
    }

    @Override
    public String saveLogo(MultipartFile file) {
        log.info("📤 Sauvegarde du logo: {}", file.getOriginalFilename());
        
        try {
            // Utiliser le chemin absolu pour être sûr
            String uploadDir = System.getProperty("user.dir") + "/uploads/logos";
            Path uploadPath = Paths.get(uploadDir);
            
            log.info("📁 Chemin du dossier: {}", uploadPath.toAbsolutePath());
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("📁 Dossier créé");
            }
            
            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = "logo_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("✅ Fichier sauvegardé: {}", filePath.toAbsolutePath());
            log.info("📎 Taille du fichier: {} bytes", Files.size(filePath));
            
            // Retourner le chemin relatif
            String relativePath = "/uploads/logos/" + filename;
            log.info("📎 Chemin retourné: {}", relativePath);
            
            return relativePath;
            
        } catch (IOException e) {
            log.error("❌ Erreur sauvegarde logo: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde du logo: " + e.getMessage());
        }
    }
    @Override
    public MarqueBlancheDTO initializeDefaultConfiguration() {
        log.info("🎨 Création de la configuration par défaut");
        
        MarqueBlanche defaultConfig = new MarqueBlanche();
        defaultConfig.setNomApplication("Plateforme de recrutement");
        defaultConfig.setLogoEntreprise("");
        defaultConfig.setCouleurPrimaire("#4f46e5");
        defaultConfig.setCouleurSecondaire("#e0e7ff");
        defaultConfig.setCouleurAccent("#818cf8");
        defaultConfig.setCouleurFond("#ffffff");
        defaultConfig.setTexteMentionsLegales("Mentions légales à compléter");
        defaultConfig.setTextePolitiqueConfidentialite("Politique de confidentialité à compléter");
        defaultConfig.setEmailContact("contact@entreprise.com");
        defaultConfig.setTelephoneContact("+21600000000");
        defaultConfig.setAdresseEntreprise("Rue de l'entreprise, Ville");
        defaultConfig.setSiteWeb("https://entreprise.com");
        defaultConfig.setAfficherChampTelephone(true);
        defaultConfig.setAfficherChampLinkedin(true);
        defaultConfig.setAfficherChampPortfolio(false);
        defaultConfig.setAfficherChampCourriel(true);
        defaultConfig.setStatutInitialCandidature("NOUVELLE");
        
        MarqueBlanche saved = repository.save(defaultConfig);
        log.info("✅ Configuration par défaut créée - ID: {}", saved.getId());
        
        return convertToDTO(saved);
    }

    @Override
    public void deleteAllAndCreateDefault() {
        log.info("🗑️ Suppression de toutes les configurations et création par défaut");
        repository.deleteAll();
        initializeDefaultConfiguration();
    }

    private MarqueBlancheDTO convertToDTO(MarqueBlanche entity) {
        MarqueBlancheDTO dto = new MarqueBlancheDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}