// com.example.recrutement.marque_blanche.service.MarqueBlancheService.java
package com.example.recrutement.marque_blanche.service;

import com.example.recrutement.marque_blanche.dto.MarqueBlancheDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MarqueBlancheService {
    
    MarqueBlancheDTO getActiveConfiguration();
    
    MarqueBlancheDTO updateConfiguration(MarqueBlancheDTO dto);
    
    String saveLogo(MultipartFile file);
    
    MarqueBlancheDTO initializeDefaultConfiguration();
    
    void deleteAllAndCreateDefault();
}