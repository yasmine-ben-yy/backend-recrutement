package com.example.recrutement.candidature.entity;

public enum PipelineStage {
    CANDIDATURE_RECUE("Candidature reçue", "bg-blue-100 text-blue-700"),
    PREQUALIFICATION("Préqualification", "bg-cyan-100 text-cyan-700"),
    ENTRETIEN_PROGRAMME("Entretien programmé", "bg-purple-100 text-purple-700"),
    ENTRETIEN_TERMINE("Entretien terminé", "bg-indigo-100 text-indigo-700"),
    EVALUATION_ENTRETIEN("Évaluation entretien", "bg-slate-100 text-slate-700"),
    SECOND_ENTRETIEN("Second entretien", "bg-fuchsia-100 text-fuchsia-700"),
    ACCEPTE("Accepté", "bg-green-100 text-green-700"),
    REFUSE("Refusé", "bg-red-100 text-red-700"),
    RECRUTE("Recruté", "bg-emerald-100 text-emerald-700");
    
    private final String label;
    private final String badgeStyle;
    
    PipelineStage(String label, String badgeStyle) {
        this.label = label;
        this.badgeStyle = badgeStyle;
    }
    
    public String getLabel() { return label; }
    public String getBadgeStyle() { return badgeStyle; }
}