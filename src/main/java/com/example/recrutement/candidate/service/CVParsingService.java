package com.example.recrutement.candidate.service;

import com.example.recrutement.candidate.dto.PythonAnalyzeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CVParsingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${python.api.url:http://localhost:8000}")
    private String pythonApiUrl;

    public CVParsingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public PythonAnalyzeResponse analyzeCV(MultipartFile file) {
        log.info("🔍 Analyse CV - Fichier: {}", file.getOriginalFilename());
        
        if (file == null || file.isEmpty()) {
            log.error("❌ Fichier CV vide");
            return new PythonAnalyzeResponse();
        }
        
        String text = extractTextFromPDF(file);
        if (text == null || text.trim().isEmpty()) {
            log.error("❌ Texte extrait vide");
            return new PythonAnalyzeResponse();
        }
        
        log.info("📄 Texte extrait: {} caractères", text.length());
        
        return analyzeText(text);
    }

    private String extractTextFromPDF(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("📄 PDFBox a extrait {} caractères", text != null ? text.length() : 0);
            return text;
        } catch (Exception e) {
            log.error("❌ Erreur extraction PDF: {}", e.getMessage());
            return null;
        }
    }

    private PythonAnalyzeResponse analyzeText(String text) {
        try {
            log.info("🤖 Envoi à FastAPI: {}", pythonApiUrl + "/analyze");
            
            // ✅ Construction simple du JSON
            String escapedText = escapeJson(text);
            String jsonBody = "{\"text\":\"" + escapedText + "\"}";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<PythonAnalyzeResponse> response = restTemplate.exchange(
                    pythonApiUrl + "/analyze",
                    HttpMethod.POST,
                    entity,
                    PythonAnalyzeResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("✅ FastAPI a répondu - name: {}", response.getBody().getName());
                return response.getBody();
            }
            
            return new PythonAnalyzeResponse();
            
        } catch (Exception e) {
            log.error("❌ Erreur appel FastAPI: {}", e.getMessage());
            return new PythonAnalyzeResponse();
        }
    }
    
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}