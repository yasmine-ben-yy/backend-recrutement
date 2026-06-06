package com.example.recrutement.candidature.exception;

public class CandidatureNotFoundException extends RuntimeException {
    public CandidatureNotFoundException(String message) {
        super(message);
    }
    
    public CandidatureNotFoundException(Long id) {
        super("Candidature non trouvée avec l'ID : " + id);
    }
}