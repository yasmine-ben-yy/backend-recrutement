package com.example.recrutement.candidate.service;

import com.example.recrutement.candidate.dto.PythonMatchResponse;
import com.example.recrutement.candidate.dto.PythonHybridMatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PythonApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${python.api.url:http://localhost:8000}")
    private String pythonApiUrl;

    public PythonApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public PythonMatchResponse calculateHybridMatchingScore(PythonHybridMatchRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<PythonMatchResponse> response = restTemplate.exchange(
                pythonApiUrl + "/match", HttpMethod.POST, entity, PythonMatchResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Erreur appel FastAPI : {}", e.getMessage());
            return new PythonMatchResponse();
        }
    }
}