package com.example.recrutement.candidature.exception;

import java.util.UUID;

public class CandidatureAlreadyExistsException extends RuntimeException {
    public CandidatureAlreadyExistsException(String message) {
        super(message);
    }
    
    public CandidatureAlreadyExistsException(Long candidateId, UUID offreId) {
        super("Le candidat avec l'ID " + candidateId + " a déjà postulé à l'offre " + offreId);
    }
}