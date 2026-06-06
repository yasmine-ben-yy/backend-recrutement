// controller/EmailStatusController.java
package com.example.recrutement.controller;

import com.example.recrutement.candidature.entity.Candidature;
import com.example.recrutement.candidature.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email-status")
@RequiredArgsConstructor
public class EmailStatusController {

    private final CandidatureRepository candidatureRepository;
    
    @GetMapping("/candidature/{id}")
    public ResponseEntity<?> getEmailStatus(@PathVariable Long id) {
        Candidature candidature = candidatureRepository.findById(id).orElse(null);
        
        if (candidature == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("emailSent", candidature.getEmailSent());
        status.put("emailSentDate", candidature.getEmailSentDate());
        status.put("emailError", candidature.getEmailError());
        status.put("emailRetryCount", candidature.getEmailRetryCount());
        
        return ResponseEntity.ok(status);
    }
}