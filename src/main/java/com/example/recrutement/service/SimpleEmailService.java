package com.example.recrutement.service;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.marque_blanche.entity.MarqueBlanche;
import com.example.recrutement.marque_blanche.repository.MarqueBlancheRepository;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.repository.OffreRepository;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class SimpleEmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final CandidateProfileRepository candidateProfileRepository;
    private final OffreRepository offreRepository;
    private final UserRepository userRepository;
    private final MarqueBlancheRepository marqueBlancheRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base.url:http://localhost:3000}")
    private String baseUrl;

    @Value("${app.email.rh-email:yasmineben779@gmail.com}")
    private String rhEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRENCH);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm", Locale.FRENCH);

    // ==================== MÉTHODES PRIVÉES ====================

    private MarqueBlanche getBrandConfig() {
        return marqueBlancheRepository.findLatest().orElse(new MarqueBlanche());
    }

    private String getCandidateEmail(Candidature candidature) {
        if (candidature == null || candidature.getCandidate() == null) return null;
        CandidateProfile profile = candidateProfileRepository.findById(candidature.getCandidate().getId()).orElse(null);
        if (profile == null || profile.getUser() == null) return null;
        User user = userRepository.findById(profile.getUser().getId()).orElse(null);
        return user != null ? user.getEmail() : null;
    }

    private CandidateProfile getCandidateProfile(Candidature candidature) {
        if (candidature == null || candidature.getCandidate() == null) return null;
        return candidateProfileRepository.findById(candidature.getCandidate().getId()).orElse(null);
    }

    private String getOffreTitre(Candidature candidature) {
        if (candidature == null || candidature.getOffre() == null) return "Poste";
        OffreEmploi offre = offreRepository.findById(candidature.getOffre().getId()).orElse(null);
        return offre != null && offre.getTitre() != null ? offre.getTitre() : "Poste";
    }

    private String traduireStatut(String statut) {
        if (statut == null) return "Inconnu";
        return switch (statut.toUpperCase()) {
            case "EN_ATTENTE" -> "En attente";
            case "A_CONTACTER" -> "À contacter";
            case "ENTRETIEN" -> "Entretien programmé";
            case "RETENUE" -> "Candidature retenue";
            case "RECRUTE" -> "Recruté";
            case "ELIMINE" -> "Non retenu";
            default -> statut.toLowerCase().replace("_", " ");
        };
    }

    private String traduireTypeEntretien(Interview.InterviewType type) {
        if (type == null) return "Non spécifié";
        return switch (type) {
            case RH -> "Entretien RH";
            case TECHNIQUE -> "Entretien Technique";
            case FINAL -> "Entretien Final";
            case PHONE -> "Entretien Téléphonique";
            case VIDEO -> "Visio-conférence";
            case PRESENTIEL -> "Entretien en présentiel";
        };
    }

    private String getStatusEmailSubject(String statut, String offreTitre) {
        MarqueBlanche brand = getBrandConfig();
        String appName = brand.getNomApplication() != null ? brand.getNomApplication() : "Plateforme";
        return switch (statut.toUpperCase()) {
            case "ENTRETIEN" -> "📅 " + appName + " - Entretien programmé pour " + offreTitre;
            case "RETENUE" -> "✅ " + appName + " - Votre candidature est retenue";
            case "RECRUTE" -> "🎉 " + appName + " - Félicitations ! Vous êtes recruté(e)";
            case "ELIMINE" -> "📧 " + appName + " - Mise à jour de votre candidature";
            default -> "📧 " + appName + " - Mise à jour du statut de votre candidature";
        };
    }

    private String getStatusTemplate(String statut) {
        return switch (statut.toUpperCase()) {
            case "ENTRETIEN" -> "email/status-interview";
            case "RETENUE" -> "email/status-retained";
            case "RECRUTE" -> "email/status-hired";
            case "ELIMINE" -> "email/status-rejected";
            default -> "email/status-update";
        };
    }

    // ==================== MÉTHODES D'ENVOI ====================

    @Async
    public void sendEmailWithBrand(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!emailEnabled) {
            log.info("📧 Email service désactivé");
            return;
        }

        try {
            MarqueBlanche brand = getBrandConfig();
            String appName = brand.getNomApplication() != null ? brand.getNomApplication() : "Plateforme recrutement";
            
            Context context = new Context();
            context.setVariable("brand", brand);
            context.setVariable("siteUrl", baseUrl);
            context.setVariable("year", LocalDateTime.now().getYear());
            
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
            
            String htmlContent = templateEngine.process(templateName, context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("✅ Email envoyé à {} - Sujet: {}", to, subject);
            
        } catch (Exception e) {
            log.error("❌ Erreur envoi email à {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String htmlContent) throws Exception {
        if (!emailEnabled) return;
        
        MarqueBlanche brand = getBrandConfig();
        String appName = brand.getNomApplication() != null ? brand.getNomApplication() : "Plateforme recrutement";
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail, appName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Override
    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        MarqueBlanche brand = getBrandConfig();
        variables.put("brand", brand);
        variables.put("siteUrl", baseUrl);
        variables.put("year", LocalDateTime.now().getYear());
        variables.forEach(context::setVariable);
        return templateEngine.process(templateName, context);
    }

    @Override
    @Async
    public void sendCandidatureConfirmation(Candidature candidature, CandidateProfile profile, OffreEmploi offre) {
        if (!emailEnabled) return;

        String candidateEmail = profile.getUser() != null ? profile.getUser().getEmail() : null;
        if (candidateEmail == null || candidateEmail.isEmpty()) {
            log.error("❌ Aucun email trouvé pour le candidat");
            return;
        }

        MarqueBlanche brand = getBrandConfig();
        String appName = brand.getNomApplication() != null ? brand.getNomApplication() : "Plateforme";
        String subject = "📝 " + appName + " - Candidature reçue pour " + offre.getTitre();

        Map<String, Object> variables = new HashMap<>();
        variables.put("prenom", profile.getPrenom());
        variables.put("nom", profile.getNom());
        variables.put("offreTitre", offre.getTitre());
        variables.put("offreLocalisation", offre.getLocalisation());
        variables.put("dateCandidature", candidature.getDateCandidature().format(DATE_TIME_FORMATTER));
        variables.put("candidatureId", candidature.getId());
        variables.put("emailContact", brand.getEmailContact() != null ? brand.getEmailContact() : rhEmail);

        sendEmailWithBrand(candidateEmail, subject, "email/candidate-confirmation", variables);
        log.info("✅ Email confirmation candidature envoyé à {}", candidateEmail);
    }

    @Override
    @Async
    public void sendRHNotification(Candidature candidature, CandidateProfile profile, OffreEmploi offre) {
        if (!emailEnabled) return;

        String subject = "📝 Nouvelle candidature - " + offre.getTitre();

        Map<String, Object> variables = new HashMap<>();
        variables.put("candidatNom", profile.getPrenom() + " " + profile.getNom());
        variables.put("candidatEmail", profile.getUser().getEmail());
        variables.put("candidatTelephone", profile.getTelephone());
        variables.put("offreTitre", offre.getTitre());
        variables.put("dateCandidature", candidature.getDateCandidature().format(DATE_TIME_FORMATTER));
        variables.put("adminUrl", baseUrl + "/rh/candidatures/" + candidature.getId());

        sendEmailWithBrand(rhEmail, subject, "email/rh-notification", variables);
        log.info("✅ Notification RH envoyée");
    }

    @Override
    @Async
    public void sendStatusChangeEmail(Candidature candidature, String ancienStatut, String nouveauStatut, String commentaire) {
        if (!emailEnabled || candidature == null) return;

        String candidateEmail = getCandidateEmail(candidature);
        if (candidateEmail == null) return;

        CandidateProfile profile = getCandidateProfile(candidature);
        if (profile == null) return;

        String offreTitre = getOffreTitre(candidature);
        String subject = getStatusEmailSubject(nouveauStatut, offreTitre);
        String templateName = getStatusTemplate(nouveauStatut);

        Map<String, Object> variables = new HashMap<>();
        variables.put("prenom", profile.getPrenom());
        variables.put("nom", profile.getNom());
        variables.put("offreTitre", offreTitre);
        variables.put("ancienStatut", traduireStatut(ancienStatut));
        variables.put("nouveauStatut", traduireStatut(nouveauStatut));
        variables.put("commentaire", commentaire != null ? commentaire : "");
        variables.put("dateChangement", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        variables.put("candidatureId", candidature.getId());

        sendEmailWithBrand(candidateEmail, subject, templateName, variables);
        log.info("✅ Email changement statut envoyé pour la candidature: {}", candidature.getId());
    }

    @Override
    @Async
    public void sendInterviewScheduledEmail(Candidature candidature, Interview interview, String commentaire) {
        if (!emailEnabled) return;

        String candidateEmail = getCandidateEmail(candidature);
        if (candidateEmail == null) return;

        CandidateProfile profile = getCandidateProfile(candidature);
        if (profile == null) return;

        String offreTitre = getOffreTitre(candidature);
        MarqueBlanche brand = getBrandConfig();
        String appName = brand.getNomApplication() != null ? brand.getNomApplication() : "Plateforme";
        String subject = "📅 " + appName + " - Entretien programmé pour " + offreTitre;

        Map<String, Object> variables = new HashMap<>();
        variables.put("prenom", profile.getPrenom());
        variables.put("nom", profile.getNom());
        variables.put("offreTitre", offreTitre);
        variables.put("dateEntretien", interview.getDateEntretien().format(DATE_FORMATTER));
        variables.put("heureEntretien", interview.getDateEntretien().format(TIME_FORMATTER));
        variables.put("dureeMinutes", interview.getDureeMinutes());
        variables.put("typeEntretien", traduireTypeEntretien(interview.getType()));
        variables.put("lieu", interview.getLieu() != null ? interview.getLieu() : "Non spécifié");
        variables.put("meetingLink", interview.getMeetingLink());
        variables.put("commentaire", commentaire);
        variables.put("candidatureId", candidature.getId());

        sendEmailWithBrand(candidateEmail, subject, "email/interview-scheduled", variables);
        log.info("✅ Email entretien programmé envoyé à {}", candidateEmail);
    }

    @Override
    @Async
    public void sendInterviewUpdatedEmail(Candidature candidature, Interview interview, String ancienneDate, String commentaire) {
        if (!emailEnabled) return;

        String candidateEmail = getCandidateEmail(candidature);
        if (candidateEmail == null) return;

        CandidateProfile profile = getCandidateProfile(candidature);
        if (profile == null) return;

        String offreTitre = getOffreTitre(candidature);
        MarqueBlanche brand = getBrandConfig();
        String appName = brand.getNomApplication() != null ? brand.getNomApplication() : "Plateforme";
        String subject = "📅 " + appName + " - Mise à jour de votre entretien";

        Map<String, Object> variables = new HashMap<>();
        variables.put("prenom", profile.getPrenom());
        variables.put("nom", profile.getNom());
        variables.put("offreTitre", offreTitre);
        variables.put("ancienneDate", ancienneDate);
        variables.put("nouvelleDate", interview.getDateEntretien().format(DATE_FORMATTER));
        variables.put("nouvelleHeure", interview.getDateEntretien().format(TIME_FORMATTER));
        variables.put("dureeMinutes", interview.getDureeMinutes());
        variables.put("typeEntretien", traduireTypeEntretien(interview.getType()));
        variables.put("lieu", interview.getLieu() != null ? interview.getLieu() : "Non spécifié");
        variables.put("meetingLink", interview.getMeetingLink());
        variables.put("commentaire", commentaire);
        variables.put("candidatureId", candidature.getId());

        sendEmailWithBrand(candidateEmail, subject, "email/interview-updated", variables);
        log.info("✅ Email entretien modifié envoyé à {}", candidateEmail);
    }

    @Override
    @Async
    public void sendInterviewCancelledEmail(Candidature candidature, Interview interview, String commentaire) {
        if (!emailEnabled) return;

        String candidateEmail = getCandidateEmail(candidature);
        if (candidateEmail == null) return;

        CandidateProfile profile = getCandidateProfile(candidature);
        if (profile == null) return;

        String offreTitre = getOffreTitre(candidature);
        MarqueBlanche brand = getBrandConfig();
        String appName = brand.getNomApplication() != null ? brand.getNomApplication() : "Plateforme";
        String subject = "❌ " + appName + " - Entretien annulé";

        Map<String, Object> variables = new HashMap<>();
        variables.put("prenom", profile.getPrenom());
        variables.put("nom", profile.getNom());
        variables.put("offreTitre", offreTitre);
        variables.put("dateEntretien", interview.getDateEntretien().format(DATE_FORMATTER));
        variables.put("heureEntretien", interview.getDateEntretien().format(TIME_FORMATTER));
        variables.put("commentaire", commentaire);
        variables.put("candidatureId", candidature.getId());

        sendEmailWithBrand(candidateEmail, subject, "email/interview-cancelled", variables);
        log.info("✅ Email entretien annulé envoyé à {}", candidateEmail);
    }
}