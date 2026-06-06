package com.example.recrutement.offre.exception;

import java.util.UUID;

public class OffreException extends RuntimeException {
    
    public OffreException(String message) {
        super(message);
    }
    
    public OffreException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static class OffreNonTrouveeException extends OffreException {
        public OffreNonTrouveeException(UUID offreId) {
            super("Offre non trouvée avec l'ID: " + offreId);
        }
    }
    
    public static class AccesNonAutoriseException extends OffreException {
        public AccesNonAutoriseException() {
            super("Accès non autorisé à cette offre");
        }
    }
    
    public static class OffreDejaPublieeException extends OffreException {
        public OffreDejaPublieeException() {
            super("L'offre est déjà publiée");
        }
    }
    
    public static class OffreNonPublieeException extends OffreException {
        public OffreNonPublieeException() {
            super("L'offre n'est pas publiée");
        }
    }
    
    public static class OffreExpireeException extends OffreException {
        public OffreExpireeException() {
            super("L'offre est expirée");
        }
    }
    
    public static class SuppressionImpossibleException extends OffreException {
        public SuppressionImpossibleException() {
            super("Impossible de supprimer une offre avec des candidatures");
        }
    }
}