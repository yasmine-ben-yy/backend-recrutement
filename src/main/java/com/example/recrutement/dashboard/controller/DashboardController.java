// com.example.recrutement.dashboard.controller.DashboardController.java
package com.example.recrutement.dashboard.controller;

import com.example.recrutement.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    @Operation(summary = "Statistiques du tableau de bord")
    public ResponseEntity<DashboardService.DashboardStatsDTO> getDashboardStats(Authentication auth) {
        String email = auth.getName();
        log.info("📊 Récupération des statistiques pour: {}", email);
        
        // Récupérer l'ID du RH (optionnel, pour filtrage futur)
        // Pour l'instant, on retourne toutes les stats
        DashboardService.DashboardStatsDTO stats = dashboardService.getStatsForRH(null);
        return ResponseEntity.ok(stats);
    }
}