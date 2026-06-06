// com.example.recrutement.dashboard.service.DashboardService.java
package com.example.recrutement.dashboard.service;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.entity.StatutCandidature;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.interview.repository.InterviewRepository;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.entity.OffreEmploi.OffreStatut;
import com.example.recrutement.offre.repository.OffreRepository;
import com.example.recrutement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final OffreRepository offreRepository;
    private final CandidatureRepository candidatureRepository;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;

    // DTO pour les statistiques
    public record DashboardStatsDTO(
        OffresStats offres,
        CandidaturesStats candidatures,
        EntretiensStats entretiens,
        PerformanceStats performance,
        List<ChartDataDTO> evolutionHebdomadaire,
        List<OffreEmploi> offresRecentes,
        List<ActiviteDTO> activitesRecentes
    ) {}

    public record OffresStats(
        int actives,
        int enAttente,
        int cloturees,
        int total,
        double evolution
    ) {}

    public record CandidaturesStats(
        int total,
        int nouvelles,
        int enAttente,
        int aContacter,
        int retenues,
        int recrutes,
        int eliminees,
        double evolution
    ) {}

    public record EntretiensStats(
        int planifies,
        int aujourdhui,
        int cetteSemaine,
        int confirmes,
        int termines,
        int annules
    ) {}

    public record PerformanceStats(
        double tauxConversion,
        double dureeMoyenneRecrutement,
        double scoreMatchingMoyen
    ) {}

    public record ChartDataDTO(
        String date,
        long offres,
        long candidatures,
        long entretiens
    ) {}

    public record ActiviteDTO(
        Long id,
        String type,
        String titre,
        String description,
        String statut,
        LocalDateTime date,
        String utilisateur
    ) {}

    public DashboardStatsDTO getStatsForRH(Long rhUserId) {
        log.info("📊 Génération des statistiques pour RH ID: {}", rhUserId);

        // Récupérer toutes les offres
        List<OffreEmploi> toutesOffres = offreRepository.findAll();
        
        List<OffreEmploi> offresActives = toutesOffres.stream()
                .filter(o -> o.getStatut() == OffreStatut.PUBLIEE)
                .collect(Collectors.toList());
        
        List<OffreEmploi> offresEnAttente = toutesOffres.stream()
                .filter(o -> o.getStatut() == OffreStatut.BROUILLON)
                .collect(Collectors.toList());
        
        List<OffreEmploi> offresCloturees = toutesOffres.stream()
                .filter(o -> o.getStatut() == OffreStatut.ARCHIVEE)
                .collect(Collectors.toList());

        // Récupérer toutes les candidatures
        List<Candidature> toutesCandidatures = candidatureRepository.findAll();
        
        List<Candidature> candidaturesNouvelles = toutesCandidatures.stream()
                .filter(c -> c.getDateCandidature() != null && 
                             c.getDateCandidature().toLocalDate().equals(LocalDate.now()))
                .collect(Collectors.toList());
        
        long candidaturesEnAttente = toutesCandidatures.stream()
                .filter(c -> c.getStatut() == StatutCandidature.EN_ATTENTE)
                .count();
        
        long candidaturesAContacter = toutesCandidatures.stream()
                .filter(c -> c.getStatut() == StatutCandidature.A_CONTACTER)
                .count();
        
        long candidaturesRetenues = toutesCandidatures.stream()
                .filter(c -> c.getStatut() == StatutCandidature.RETENUE)
                .count();
        
        long candidaturesRecrutes = toutesCandidatures.stream()
                .filter(c -> c.getStatut() == StatutCandidature.RECRUTE)
                .count();
        
        long candidaturesEliminees = toutesCandidatures.stream()
                .filter(c -> c.getStatut() == StatutCandidature.ELIMINE)
                .count();

        // Statistiques entretiens
        List<Interview> tousEntretiens = interviewRepository.findAll();
        LocalDate today = LocalDate.now();
        
        long entretiensPlanifies = tousEntretiens.stream()
                .filter(i -> i.getStatut() == Interview.InterviewStatus.PLANIFIE)
                .count();
        
        long entretiensAujourdhui = tousEntretiens.stream()
                .filter(i -> i.getDateEntretien() != null && 
                             i.getDateEntretien().toLocalDate().equals(today))
                .count();
        
        long entretiensCetteSemaine = tousEntretiens.stream()
                .filter(i -> {
                    if (i.getDateEntretien() == null) return false;
                    LocalDate date = i.getDateEntretien().toLocalDate();
                    LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
                })
                .count();
        
        long entretiensConfirmes = tousEntretiens.stream()
                .filter(i -> i.getStatut() == Interview.InterviewStatus.CONFIRME)
                .count();
        
        long entretiensTermines = tousEntretiens.stream()
                .filter(i -> i.getStatut() == Interview.InterviewStatus.TERMINE)
                .count();
        
        long entretiensAnnules = tousEntretiens.stream()
                .filter(i -> i.getStatut() == Interview.InterviewStatus.ANNULE)
                .count();

        // Calcul du taux de conversion
        double tauxConversion = toutesOffres.isEmpty() ? 0 :
                (double) offresCloturees.size() / toutesOffres.size() * 100;

        // Calcul de la durée moyenne de recrutement
        double dureeMoyenne = calculerDureeMoyenneRecrutement(toutesCandidatures);

        // Score matching moyen - CORRIGÉ
        double scoreMatchingMoyen = toutesCandidatures.stream()
                .filter(c -> c.getMatchingScore() != null)
                .mapToDouble(c -> c.getMatchingScore().doubleValue())
                .average()
                .orElse(0.0);

        // Évolution hebdomadaire
        List<ChartDataDTO> evolution = genererEvolutionHebdomadaire();

        // Offres récentes (dernières 5)
        List<OffreEmploi> offresRecentes = toutesOffres.stream()
                .filter(o -> o.getDatePublication() != null)
                .sorted((a, b) -> b.getDatePublication().compareTo(a.getDatePublication()))
                .limit(5)
                .collect(Collectors.toList());

        // Activités récentes
        List<ActiviteDTO> activites = genererActivitesRecentes();

        return new DashboardStatsDTO(
            new OffresStats(
                offresActives.size(),
                offresEnAttente.size(),
                offresCloturees.size(),
                toutesOffres.size(),
                calculerEvolutionOffres(toutesOffres)
            ),
            new CandidaturesStats(
                toutesCandidatures.size(),
                candidaturesNouvelles.size(),
                (int) candidaturesEnAttente,
                (int) candidaturesAContacter,
                (int) candidaturesRetenues,
                (int) candidaturesRecrutes,
                (int) candidaturesEliminees,
                calculerEvolutionCandidatures(toutesCandidatures)
            ),
            new EntretiensStats(
                (int) entretiensPlanifies,
                (int) entretiensAujourdhui,
                (int) entretiensCetteSemaine,
                (int) entretiensConfirmes,
                (int) entretiensTermines,
                (int) entretiensAnnules
            ),
            new PerformanceStats(
                Math.round(tauxConversion * 10) / 10.0,
                Math.round(dureeMoyenne * 10) / 10.0,
                Math.round(scoreMatchingMoyen * 10) / 10.0
            ),
            evolution,
            offresRecentes,
            activites
        );
    }

    private double calculerDureeMoyenneRecrutement(List<Candidature> candidatures) {
        List<Long> durees = new ArrayList<>();
        
        for (Candidature c : candidatures) {
            if (c.getStatut() == StatutCandidature.RECRUTE && c.getDateCandidature() != null) {
                LocalDateTime dateCandidature = c.getDateCandidature();
                LocalDateTime dateRecrutement = dateCandidature.plusDays(21); // Valeur par défaut
                
                // Si vous avez une date de mise à jour
                if (c.getUpdatedAt() != null) {
                    dateRecrutement = c.getUpdatedAt();
                }
                
                long jours = ChronoUnit.DAYS.between(dateCandidature, dateRecrutement);
                if (jours > 0 && jours < 365) {
                    durees.add(jours);
                }
            }
        }
        
        return durees.stream().mapToLong(Long::longValue).average().orElse(21.0);
    }

    private double calculerEvolutionOffres(List<OffreEmploi> offres) {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusDays(60);
        
        long offresMoisDernier = offres.stream()
                .filter(o -> o.getDatePublication() != null && o.getDatePublication().isAfter(monthAgo))
                .count();
        
        long offresMoisPrecedent = offres.stream()
                .filter(o -> o.getDatePublication() != null && 
                             o.getDatePublication().isAfter(twoMonthsAgo) && 
                             o.getDatePublication().isBefore(monthAgo))
                .count();
        
        if (offresMoisPrecedent == 0) return offresMoisDernier > 0 ? 100 : 0;
        return ((double) (offresMoisDernier - offresMoisPrecedent) / offresMoisPrecedent) * 100;
    }

    private double calculerEvolutionCandidatures(List<Candidature> candidatures) {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusDays(60);
        
        long candMoisDernier = candidatures.stream()
                .filter(c -> c.getDateCandidature() != null && c.getDateCandidature().isAfter(monthAgo))
                .count();
        
        long candMoisPrecedent = candidatures.stream()
                .filter(c -> c.getDateCandidature() != null && 
                             c.getDateCandidature().isAfter(twoMonthsAgo) && 
                             c.getDateCandidature().isBefore(monthAgo))
                .count();
        
        if (candMoisPrecedent == 0) return candMoisDernier > 0 ? 100 : 0;
        return ((double) (candMoisDernier - candMoisPrecedent) / candMoisPrecedent) * 100;
    }

    private List<ChartDataDTO> genererEvolutionHebdomadaire() {
        List<ChartDataDTO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            long offresJour = offreRepository.countByDatePublicationBetween(startOfDay, endOfDay);
            long candidaturesJour = candidatureRepository.countByDateCandidatureBetween(startOfDay, endOfDay);
            long entretiensJour = interviewRepository.countByDateEntretienBetween(startOfDay, endOfDay);
            
            String nomJour = switch (date.getDayOfWeek().getValue()) {
                case 1 -> "Lun";
                case 2 -> "Mar";
                case 3 -> "Mer";
                case 4 -> "Jeu";
                case 5 -> "Ven";
                case 6 -> "Sam";
                default -> "Dim";
            };
            
            result.add(new ChartDataDTO(
                nomJour,
                offresJour,
                candidaturesJour,
                entretiensJour
            ));
        }
        return result;
    }

    private List<ActiviteDTO> genererActivitesRecentes() {
        List<ActiviteDTO> activites = new ArrayList<>();
        
        // Dernières candidatures
        List<Candidature> dernieresCandidatures = candidatureRepository.findTop5ByOrderByDateCandidatureDesc();
        if (dernieresCandidatures != null) {
            for (Candidature c : dernieresCandidatures) {
                if (c.getCandidate() != null) {
                    String nomCandidat = (c.getCandidate().getPrenom() != null ? c.getCandidate().getPrenom() : "") + 
                                         " " + (c.getCandidate().getNom() != null ? c.getCandidate().getNom() : "");
                    activites.add(new ActiviteDTO(
                        c.getId(),
                        "CANDIDATURE",
                        "Nouvelle candidature",
                        nomCandidat.trim() + " a postulé",
                        c.getStatut() != null ? c.getStatut().name() : "INCONNU",
                        c.getDateCandidature(),
                        nomCandidat.trim()
                    ));
                }
            }
        }
        
        // Derniers entretiens
        List<Interview> derniersEntretiens = interviewRepository.findTop5ByOrderByDateEntretienDesc();
        if (derniersEntretiens != null) {
            for (Interview i : derniersEntretiens) {
                if (i.getCandidature() != null && i.getCandidature().getCandidate() != null) {
                    String nomCandidat = (i.getCandidature().getCandidate().getPrenom() != null ? i.getCandidature().getCandidate().getPrenom() : "") + 
                                         " " + (i.getCandidature().getCandidate().getNom() != null ? i.getCandidature().getCandidate().getNom() : "");
                    activites.add(new ActiviteDTO(
                        i.getId(),
                        "ENTRETIEN",
                        "Entretien programmé",
                        nomCandidat.trim() + " - " + (i.getType() != null ? i.getType().name() : "ENTRETIEN"),
                        i.getStatut() != null ? i.getStatut().name() : "PLANIFIE",
                        i.getDateEntretien(),
                        i.getCreatedBy() != null ? i.getCreatedBy().getEmail() : "RH"
                    ));
                }
            }
        }
        
        // Trier par date
        activites.sort((a, b) -> {
            if (a.date() == null && b.date() == null) return 0;
            if (a.date() == null) return 1;
            if (b.date() == null) return -1;
            return b.date().compareTo(a.date());
        });
        
        return activites.stream().limit(10).collect(Collectors.toList());
    }
}