// com.example.recrutement.security.JwtService.java
package com.example.recrutement.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.recrutement.user.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET = "my-super-secret-key-for-jwt-my-super-secret-key-my-super-secret-key";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    
    // Durée de validité du token : 24 heures
    private static final long JWT_EXPIRATION = 1000 * 60 * 60 * 24;

    /**
     * Génère un token JWT pour un utilisateur (version complète avec claims)
     */
    public String generateToken(User user) {
        System.out.println("🔑 Generating JWT for user: " + user.getEmail() + " with role: " + user.getRole());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + user.getRole().name());
        claims.put("email", user.getEmail());
        
        if (user.getName() != null) {
            claims.put("name", user.getName());
        }
        if (user.getPictureUrl() != null) {
            claims.put("picture", user.getPictureUrl());
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur (email) du token
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.out.println("❌ Error extracting username from JWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrait le rôle du token
     */
    public String extractRole(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifie si le token est valide (non expiré)
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            String role = claims.get("role", String.class);
            System.out.println("✅ Valid JWT for user: " + claims.getSubject() + " | Role in token: " + role);
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.out.println("❌ Invalid JWT: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le token est valide et correspond au UserDetails
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername())) && isTokenValid(token);
    }

    /**
     * Vérifie si le token est expiré
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait une claim spécifique du token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse le token JWT et retourne les claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}