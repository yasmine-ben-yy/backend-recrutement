// com.example.recrutement.marque_blanche.controller.MarqueBlancheController.java
package com.example.recrutement.marque_blanche.controller;

import com.example.recrutement.marque_blanche.dto.MarqueBlancheDTO;
import com.example.recrutement.marque_blanche.service.MarqueBlancheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/marque-blanche")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarqueBlancheController {

    private final MarqueBlancheService marqueBlancheService;

    @GetMapping("/active")
    public ResponseEntity<MarqueBlancheDTO> getActiveConfiguration() {
        return ResponseEntity.ok(marqueBlancheService.getActiveConfiguration());
    }

    @PutMapping("/update")
    public ResponseEntity<MarqueBlancheDTO> updateConfiguration(@RequestBody MarqueBlancheDTO dto) {
        return ResponseEntity.ok(marqueBlancheService.updateConfiguration(dto));
    }

    @PostMapping("/upload-logo")
    public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            String logoUrl = marqueBlancheService.saveLogo(file);
            return ResponseEntity.ok(Map.of("url", logoUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/init")
    public ResponseEntity<MarqueBlancheDTO> initializeDefaultConfiguration() {
        return ResponseEntity.ok(marqueBlancheService.initializeDefaultConfiguration());
    }
    
    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetConfiguration() {
        marqueBlancheService.deleteAllAndCreateDefault();
        return ResponseEntity.ok().build();
    }
}