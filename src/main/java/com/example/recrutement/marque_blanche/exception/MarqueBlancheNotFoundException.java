// com.example.recrutement.marque_blanche.exception.MarqueBlancheNotFoundException.java
package com.example.recrutement.marque_blanche.exception;

public class MarqueBlancheNotFoundException extends RuntimeException {
    
    public MarqueBlancheNotFoundException(String message) {
        super(message);
    }
    
    public MarqueBlancheNotFoundException(Long id) {
        super("Configuration marque blanche non trouvée avec l'id : " + id);
    }
}