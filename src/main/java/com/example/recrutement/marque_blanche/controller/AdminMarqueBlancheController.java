// com.example.recrutement.marque_blanche.controller.AdminMarqueBlancheController.java
package com.example.recrutement.marque_blanche.controller;

import com.example.recrutement.marque_blanche.dto.MarqueBlancheDTO;
import com.example.recrutement.marque_blanche.service.MarqueBlancheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/marque-blanche")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN_MB')")
public class AdminMarqueBlancheController {

    private final MarqueBlancheService marqueBlancheService;

    @GetMapping
    public ResponseEntity<MarqueBlancheDTO> getConfig() {
        return ResponseEntity.ok(marqueBlancheService.getActiveConfiguration());
    }

    @PutMapping
    public ResponseEntity<MarqueBlancheDTO> updateConfig(@RequestBody MarqueBlancheDTO dto) {
        return ResponseEntity.ok(marqueBlancheService.updateConfiguration(dto));
    }
}