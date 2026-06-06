package com.example.recrutement.ai.dto;

import java.util.List;

public class AIDetailedScores {
    private Double globalScore;
    private Double semanticScore;
    private Double skillsScore;
    private Double degreeScore;
    private Double experienceScore;
    private Double titleScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;

    // Constructeurs
    public AIDetailedScores() {}

    public AIDetailedScores(Double globalScore, Double semanticScore, Double skillsScore, 
                           Double degreeScore, Double experienceScore, Double titleScore,
                           List<String> matchedSkills, List<String> missingSkills) {
        this.globalScore = globalScore;
        this.semanticScore = semanticScore;
        this.skillsScore = skillsScore;
        this.degreeScore = degreeScore;
        this.experienceScore = experienceScore;
        this.titleScore = titleScore;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
    }

    // Getters et Setters
    public Double getGlobalScore() {
        return globalScore;
    }

    public void setGlobalScore(Double globalScore) {
        this.globalScore = globalScore;
    }

    public Double getSemanticScore() {
        return semanticScore;
    }

    public void setSemanticScore(Double semanticScore) {
        this.semanticScore = semanticScore;
    }

    public Double getSkillsScore() {
        return skillsScore;
    }

    public void setSkillsScore(Double skillsScore) {
        this.skillsScore = skillsScore;
    }

    public Double getDegreeScore() {
        return degreeScore;
    }

    public void setDegreeScore(Double degreeScore) {
        this.degreeScore = degreeScore;
    }

    public Double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(Double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public Double getTitleScore() {
        return titleScore;
    }

    public void setTitleScore(Double titleScore) {
        this.titleScore = titleScore;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    @Override
    public String toString() {
        return "AIDetailedScores{" +
                "globalScore=" + globalScore +
                ", semanticScore=" + semanticScore +
                ", skillsScore=" + skillsScore +
                ", degreeScore=" + degreeScore +
                ", experienceScore=" + experienceScore +
                ", titleScore=" + titleScore +
                ", matchedSkills=" + matchedSkills +
                ", missingSkills=" + missingSkills +
                '}';
    }
}