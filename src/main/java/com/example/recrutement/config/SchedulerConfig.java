package com.example.recrutement.config;

import com.example.recrutement.offre.service.OffreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {

    private final OffreService offreService;

    /**
     * Clôture automatique des offres expirées - Tous les jours à minuit
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cloturerOffresExpirees() {
        log.info("🕐 Exécution du scheduler: clôture des offres expirées");
        offreService.cloturerOffresExpirees();
    }
    
    /**
     * Vérification des offres qui expirent bientôt - Tous les jours à 9h
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void verifierOffresExpirant() {
        log.info("🕐 Exécution du scheduler: vérification des offres qui expirent");
        offreService.checkExpiringOffres();
    }
}