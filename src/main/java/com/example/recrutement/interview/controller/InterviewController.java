// com.example.recrutement.interview.controller.InterviewController.java
package com.example.recrutement.interview.controller;

import com.example.recrutement.interview.dto.AddNoteRequest;
import com.example.recrutement.interview.dto.CreateInterviewDTO;
import com.example.recrutement.interview.dto.InterviewResponse;
import com.example.recrutement.interview.dto.InterviewRequest;
import com.example.recrutement.interview.dto.UpdateStatusRequest;
import com.example.recrutement.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<InterviewResponse> createInterview(
            @Valid @RequestBody CreateInterviewDTO dto,
            Authentication auth) {
        log.info("📅 Création d'un entretien - Candidature: {}, Type: {}", 
                 dto.getCandidatureId(), dto.getType());
        
        String userEmail = auth.getName();
        
        InterviewRequest request = new InterviewRequest();
        request.setCandidatureId(dto.getCandidatureId());
        request.setType(com.example.recrutement.interview.entity.Interview.InterviewType.valueOf(dto.getType()));
        request.setDate(dto.getDateEntretien());
        request.setDuree(dto.getDureeMinutes());
        request.setLieu(dto.getLieu());
        request.setMeetingLink(dto.getMeetingLink());
        request.setNotes(dto.getNotes());
        
        InterviewResponse interview = interviewService.createInterview(request, userEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(interview);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<List<InterviewResponse>> getAllInterviews(Authentication auth) {
        log.info("📋 Récupération de tous les entretiens");
        
        List<InterviewResponse> interviews = interviewService.getAllInterviews();
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB', 'CANDIDAT')")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id) {
        log.info("📋 Récupération de l'entretien: {}", id);
        
        InterviewResponse interview = interviewService.getInterviewById(id);
        return ResponseEntity.ok(interview);
    }

    // 🔵 Endpoint pour RH/ADMIN - Accès à toutes les candidatures
    @GetMapping("/candidature/{candidatureId}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<List<InterviewResponse>> getByCandidatureForRH(@PathVariable Long candidatureId) {
        log.info("📋 RH/ADMIN: Récupération des entretiens pour candidature: {}", candidatureId);
        
        List<InterviewResponse> interviews = interviewService.getByCandidature(candidatureId);
        return ResponseEntity.ok(interviews);
    }

    // 🟢 Endpoint pour CANDIDAT - Ne voit que ses propres entretiens
    @GetMapping("/candidat/{candidatId}")
    @PreAuthorize("hasRole('CANDIDAT') and #candidatId == authentication.principal.id")
    public ResponseEntity<List<InterviewResponse>> getByCandidat(@PathVariable Long candidatId, Authentication auth) {
        log.info("👤 CANDIDAT: Récupération des entretiens pour candidat: {}", candidatId);
        
        List<InterviewResponse> interviews = interviewService.getInterviewsByCandidat(candidatId);
        return ResponseEntity.ok(interviews);
    }
    
    // 🟢 NOUVEAU: Endpoint pratique pour le candidat connecté
    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDAT')")
    @Operation(summary = "Mes entretiens", description = "Récupère tous les entretiens du candidat connecté")
    public ResponseEntity<List<InterviewResponse>> getMyInterviews(Authentication auth) {
        String email = auth.getName();
        log.info("👤 CANDIDAT: Récupération de mes entretiens pour: {}", email);
        
        List<InterviewResponse> interviews = interviewService.getInterviewsByCandidatEmail(email);
        return ResponseEntity.ok(interviews);
    }
    
    // 🟢 Endpoint pour candidat avec ID de candidature (vérifie la propriété)
    @GetMapping("/candidature/{candidatureId}/my")
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<List<InterviewResponse>> getMyInterviewsByCandidature(
            @PathVariable Long candidatureId,
            Authentication auth) {
        String email = auth.getName();
        log.info("👤 CANDIDAT: Récupération des entretiens pour ma candidature: {}", candidatureId);
        
        
        List<InterviewResponse> interviews = interviewService.getByCandidature(candidatureId);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/offre/{offreId}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<List<InterviewResponse>> getByOffre(@PathVariable UUID offreId) {
        log.info("📋 Récupération des entretiens pour offre: {}", offreId);
        
        List<InterviewResponse> interviews = interviewService.getInterviewsByOffre(offreId);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<List<InterviewResponse>> getUpcomingInterviews() {
        log.info("📋 Récupération des entretiens à venir");
        
        List<InterviewResponse> interviews = interviewService.getUpcomingInterviews();
        return ResponseEntity.ok(interviews);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody InterviewRequest request,
            Authentication auth) {
        log.info("✏️ Mise à jour de l'entretien: {}", id);
        
        String userEmail = auth.getName();
        InterviewResponse interview = interviewService.updateInterview(id, request, userEmail);
        return ResponseEntity.ok(interview);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<InterviewResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication auth) {
        log.info("🔄 Mise à jour du statut de l'entretien {}: {}", id, request.getStatut());
        
        String userEmail = auth.getName();
        InterviewResponse interview = interviewService.updateStatus(id, request.getStatut(), userEmail);
        return ResponseEntity.ok(interview);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('CANDIDAT')")
    public ResponseEntity<InterviewResponse> confirmInterview(@PathVariable Long id, Authentication auth) {
        log.info("✅ Confirmation de l'entretien par le candidat: {}", id);
        
        String userEmail = auth.getName();
        InterviewResponse interview = interviewService.confirmInterview(id, userEmail);
        return ResponseEntity.ok(interview);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB', 'CANDIDAT')")
    public ResponseEntity<InterviewResponse> cancelInterview(@PathVariable Long id, Authentication auth) {
        log.info("❌ Annulation de l'entretien: {}", id);
        
        String userEmail = auth.getName();
        InterviewResponse interview = interviewService.cancelInterview(id, userEmail);
        return ResponseEntity.ok(interview);
    }

    @PatchMapping("/{id}/postpone")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<InterviewResponse> postponeInterview(
            @PathVariable Long id,
            @RequestParam LocalDateTime newDate,
            Authentication auth) {
        log.info("⏰ Report de l'entretien {} à {}", id, newDate);
        
        String userEmail = auth.getName();
        InterviewResponse interview = interviewService.postponeInterview(id, newDate, userEmail);
        return ResponseEntity.ok(interview);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id, Authentication auth) {
        log.info("🗑️ Suppression de l'entretien: {}", id);
        
        String userEmail = auth.getName();
        interviewService.deleteInterview(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<InterviewResponse> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequest request,
            Authentication auth) {
        log.info("📝 Ajout d'une note à l'entretien: {}", id);
        
        String userEmail = auth.getName();
        InterviewResponse interview = interviewService.addNote(id, request.getContenu(), userEmail);
        return ResponseEntity.ok(interview);
    }

    @PostMapping("/{id}/commentaires")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN_MB')")
    public ResponseEntity<InterviewResponse> addCommentaire(
            @PathVariable Long id,
            @RequestBody String contenu,
            Authentication auth) {
        log.info("💬 Ajout d'un commentaire à l'entretien: {}", id);
        
        String userEmail = auth.getName();
        InterviewResponse interview = interviewService.addCommentaire(id, contenu, userEmail);
        return ResponseEntity.ok(interview);
    }
}