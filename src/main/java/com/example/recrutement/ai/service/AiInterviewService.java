package com.example.recrutement.ai.service;

import com.example.recrutement.ai.dto.AiQuestionsRequest;
import com.example.recrutement.ai.dto.AiSummaryRequest;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.interview.entity.Interview;
import com.example.recrutement.interview.entity.InterviewQuestion;
import com.example.recrutement.interview.repository.InterviewQuestionRepository;
import com.example.recrutement.interview.repository.InterviewRepository;
import com.example.recrutement.offre.entity.OffreEmploi;
import com.example.recrutement.offre.repository.OffreRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiInterviewService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    
    @Value("${ai.ollama.model:phi3}")
    private String model;
    
    private final CandidateProfileRepository candidateProfileRepository;
    private final OffreRepository offreRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    
    /**
     * Génère des questions d'entretien personnalisées
     */
    public List<InterviewQuestion> generateInterviewQuestions(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));
        
        CandidateProfile candidat = interview.getCandidature().getCandidate();
        OffreEmploi offre = interview.getCandidature().getOffre();
        
        String prompt = buildQuestionsPrompt(candidat, offre);
        String response = callOllama(prompt);
        
        List<InterviewQuestion> questions = parseQuestionsResponse(response, interview);
        
        // Supprimer les anciennes questions et sauvegarder les nouvelles
        interviewQuestionRepository.deleteByInterviewId(interviewId);
        questions = interviewQuestionRepository.saveAll(questions);
        
        return questions;
    }
    
    /**
     * Génère un résumé IA du candidat
     */
    public String generateCandidateSummary(Long candidatId, UUID offreId) {
        CandidateProfile candidat = candidateProfileRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        String prompt = buildSummaryPrompt(candidat, offre);
        return callOllama(prompt);
    }
    
    /**
     * Analyse le CV et retourne les compétences, matching score, etc.
     */
    public Map<String, Object> analyzeCV(Long candidatId, UUID offreId) {
        CandidateProfile candidat = candidateProfileRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));
        OffreEmploi offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        String prompt = buildCVAnalysisPrompt(candidat, offre);
        String response = callOllama(prompt);
        
        Map<String, Object> result = new HashMap<>();
        try {
            JsonNode json = objectMapper.readTree(response);
            result.put("skills", extractSkills(json));
            result.put("matchingScore", calculateMatchingScore(candidat, offre));
            result.put("analysis", response);
        } catch (Exception e) {
            log.error("Erreur parsing analyse CV: {}", e.getMessage());
            result.put("analysis", response);
            result.put("matchingScore", 0);
        }
        return result;
    }
    
    private String callOllama(String prompt) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("prompt", prompt);
            request.put("stream", false);
            request.put("options", Map.of("temperature", 0.7, "max_tokens", 2000));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaUrl + "/api/generate",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("response").asText();
            
        } catch (Exception e) {
            log.error("Erreur appel Ollama: {}", e.getMessage());
            return "Erreur lors de la génération IA. Vérifiez que Ollama est démarré.";
        }
    }
    
    private String buildQuestionsPrompt(CandidateProfile candidat, OffreEmploi offre) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un expert en recrutement technique. Génére des questions d'entretien pour un candidat postulant au poste suivant.\n\n");
        sb.append("OFFRE D'EMPLOI:\n");
        sb.append("Titre: ").append(offre.getTitre()).append("\n");
        sb.append("Description: ").append(offre.getDescription()).append("\n");
        sb.append("Compétences requises: ").append(String.join(", ", offre.getCompetencesRequises())).append("\n");
        sb.append("Expérience requise: ").append(offre.getExperienceRequise()).append(" ans\n");
        sb.append("Niveau d'étude: ").append(offre.getNiveauEtude()).append("\n\n");
        
        sb.append("CV DU CANDIDAT:\n");
        sb.append("Titre: ").append(candidat.getTitreProfessionnel() != null ? candidat.getTitreProfessionnel() : "Non spécifié").append("\n");
        sb.append("Compétences: ").append(candidat.getCompetencesAsStrings()).append("\n");
        sb.append("Expérience: ").append(candidat.getExperienceAnnees()).append(" ans\n");
        sb.append("Diplôme: ").append(candidat.getNiveauEtude()).append("\n\n");
        
        sb.append("Génère 6 questions d'entretien au format JSON suivant:\n");
        sb.append("[\n");
        sb.append("  {\"type\": \"TECHNIQUE\", \"competence\": \"Nom compétence\", \"question\": \"Question technique détaillée\"},\n");
        sb.append("  {\"type\": \"SOFT_SKILL\", \"competence\": \"Soft skill ciblé\", \"question\": \"Question soft skills\"},\n");
        sb.append("  {\"type\": \"MOTIVATION\", \"question\": \"Question sur la motivation\"},\n");
        sb.append("  {\"type\": \"SITUATION\", \"question\": \"Question de mise en situation\"}\n");
        sb.append("]\n");
        sb.append("Ne réponds que par le JSON, sans texte avant ou après.");
        
        return sb.toString();
    }
    
    private String buildSummaryPrompt(CandidateProfile candidat, OffreEmploi offre) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un assistant RH. Résume le profil du candidat pour le poste suivant:\n\n");
        sb.append("POSTE: ").append(offre.getTitre()).append("\n");
        sb.append("DESCRIPTION: ").append(offre.getDescription()).append("\n\n");
        sb.append("PROFIL CANDIDAT:\n");
        sb.append("Nom: ").append(candidat.getPrenom()).append(" ").append(candidat.getNom()).append("\n");
        sb.append("Titre: ").append(candidat.getTitreProfessionnel()).append("\n");
        sb.append("Compétences: ").append(candidat.getCompetencesAsStrings()).append("\n");
        sb.append("Expérience: ").append(candidat.getExperienceAnnees()).append(" ans\n");
        sb.append("Diplôme: ").append(candidat.getNiveauEtude()).append("\n\n");
        sb.append("Génère un résumé professionnel (3-5 phrases) incluant:\n");
        sb.append("- Points forts du candidat\n");
        sb.append("- Compétences clés\n");
        sb.append("- Adéquation avec le poste\n");
        sb.append("- Recommandation RH\n");
        
        return sb.toString();
    }
    
    private String buildCVAnalysisPrompt(CandidateProfile candidat, OffreEmploi offre) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyse le CV du candidat par rapport à l'offre d'emploi.\n\n");
        sb.append("OFFRE: ").append(offre.getTitre()).append("\n");
        sb.append("Compétences requises: ").append(String.join(", ", offre.getCompetencesRequises())).append("\n");
        sb.append("EXPÉRIENCE REQUISE: ").append(offre.getExperienceRequise()).append(" ans\n\n");
        
        sb.append("CV CANDIDAT:\n");
        sb.append("Compétences: ").append(candidat.getCompetencesAsStrings()).append("\n");
        sb.append("Expérience: ").append(candidat.getExperienceAnnees()).append(" ans\n\n");
        
        sb.append("Réponds au format JSON avec les clés: matching_score (0-100), competences_trouvees, competences_manquantes, recommandation\n");
        
        return sb.toString();
    }
    
    private List<InterviewQuestion> parseQuestionsResponse(String response, Interview interview) {
        List<InterviewQuestion> questions = new ArrayList<>();
        try {
            // Nettoyer la réponse (enlever les markdown éventuels)
            String clean = response.trim();
            if (clean.startsWith("```json")) {
                clean = clean.substring(7);
            }
            if (clean.startsWith("```")) {
                clean = clean.substring(3);
            }
            if (clean.endsWith("```")) {
                clean = clean.substring(0, clean.length() - 3);
            }
            
            JsonNode array = objectMapper.readTree(clean);
            int order = 0;
            for (JsonNode node : array) {
                InterviewQuestion question = InterviewQuestion.builder()
                        .interview(interview)
                        .question(node.get("question").asText())
                        .typeQuestion(InterviewQuestion.QuestionType.valueOf(node.get("type").asText()))
                        .competence(node.has("competence") ? node.get("competence").asText() : null)
                        .orderIndex(order++)
                        .build();
                questions.add(question);
            }
        } catch (Exception e) {
            log.error("Erreur parsing questions: {}", e.getMessage());
            // Fallback: créer une question par défaut
            InterviewQuestion fallback = InterviewQuestion.builder()
                    .interview(interview)
                    .question("Pouvez-vous décrire votre expérience pertinente pour ce poste ?")
                    .typeQuestion(InterviewQuestion.QuestionType.MOTIVATION)
                    .orderIndex(0)
                    .build();
            questions.add(fallback);
        }
        return questions;
    }
    
    private List<String> extractSkills(JsonNode json) {
        List<String> skills = new ArrayList<>();
        if (json.has("competences_trouvees")) {
            JsonNode skillsNode = json.get("competences_trouvees");
            if (skillsNode.isArray()) {
                for (JsonNode s : skillsNode) {
                    skills.add(s.asText());
                }
            }
        }
        return skills;
    }
    
    private double calculateMatchingScore(CandidateProfile candidat, OffreEmploi offre) {
        // Matching simple basé sur les compétences
        List<String> required = offre.getCompetencesRequises();
        List<String> candidateSkills = candidat.getCompetencesAsStrings().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        
        if (required.isEmpty()) return 50.0;
        
        long match = required.stream()
                .filter(r -> candidateSkills.stream().anyMatch(c -> c.contains(r.toLowerCase()) || r.toLowerCase().contains(c)))
                .count();
        
        return (match * 100.0) / required.size();
    }
}