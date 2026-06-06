// com.example.recrutement.offre.scheduler.OffreScheduler.java
package com.example.recrutement.offre.scheduler;

import com.example.recrutement.offre.service.OffreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OffreScheduler {

    private final OffreService offreService;

    /**
     * Vérification quotidienne à 9h00 des offres qui expirent
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringOffres() {
        log.info("🔄 Démarrage de la vérification des offres expirant bientôt...");
        try {
            offreService.checkExpiringOffres();
            log.info("✅ Vérification des offres terminée avec succès");
        } catch (Exception e) {
            log.error("❌ Erreur lors de la vérification des offres: {}", e.getMessage());
        }
    }
}