// com.example.recrutement.service.IEmailService.java
package com.example.recrutement.service;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.offre.entity.OffreEmploi;
import java.util.Map;

public interface IEmailService {
    
    void sendCandidatureConfirmation(Candidature candidature, CandidateProfile profile, OffreEmploi offre);
    
    void sendRHNotification(Candidature candidature, CandidateProfile profile, OffreEmploi offre);
    
    void sendStatusChangeEmail(Candidature candidature, String ancienStatut, String nouveauStatut, String commentaire);
    
    void sendInterviewScheduledEmail(Candidature candidature, Interview interview, String commentaire);
    
    void sendInterviewUpdatedEmail(Candidature candidature, Interview interview, String ancienneDate, String commentaire);
    
    void sendInterviewCancelledEmail(Candidature candidature, Interview interview, String commentaire);
    
    void sendEmail(String to, String subject, String htmlContent) throws Exception;
    
    // ✅ Ajouter cette méthode
    String renderTemplate(String templateName, Map<String, Object> variables);
}