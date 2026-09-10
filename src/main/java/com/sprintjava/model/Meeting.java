package com.sprintjava.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Meeting {

    private Long id;
    private Long clientId;
    private String sourceFilename;
    private LocalDateTime createdAt;
    private String triage;
    private String selectedAgentsJson;
    private String finalReportJson;
    private String reportsJson;
    private String reviewJson;

    public Meeting() {
    }

    public Meeting(Long clientId, String sourceFilename) {
        this.clientId = clientId;
        this.sourceFilename = sourceFilename;
        this.triage = "";
        this.selectedAgentsJson = "[]";
        this.finalReportJson = "{}";
        this.reportsJson = "[]";
        this.reviewJson = "{}";
        this.createdAt = LocalDateTime.now();
    }

    public Meeting(Long id, Long clientId, String sourceFilename, LocalDateTime createdAt, String triage,
                   String selectedAgentsJson, String finalReportJson, String reportsJson, String reviewJson) {
        this.id = id;
        this.clientId = clientId;
        this.sourceFilename = sourceFilename;
        this.createdAt = createdAt;
        this.triage = triage;
        this.selectedAgentsJson = selectedAgentsJson;
        this.finalReportJson = finalReportJson;
        this.reportsJson = reportsJson;
        this.reviewJson = reviewJson;
    }

    /**
     * O relatório final vem do /analyze do serviço Python (campo final_report).
     * Uma reunião só é considerada "analisada" quando esse JSON não está vazio.
     */
    public boolean possuiAnaliseCompleta() {
        if (finalReportJson == null) {
            return false;
        }
        String semEspacos = finalReportJson.trim();
        return !semEspacos.isEmpty() && !semEspacos.equals("{}");
    }

    /**
     * Conta quantos agentes especialistas o Python selecionou para essa
     * transcrição (campo selected_agents, um array JSON de strings).
     */
    public int quantidadeAgentesSelecionados() {
        if (selectedAgentsJson == null) {
            return 0;
        }
        String conteudo = selectedAgentsJson.trim();
        if (conteudo.length() < 3) {
            return 0;
        }
        conteudo = conteudo.substring(1, conteudo.length() - 1).trim();
        if (conteudo.isEmpty()) {
            return 0;
        }
        return conteudo.split(",").length;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTriage() {
        return triage;
    }

    public void setTriage(String triage) {
        this.triage = triage;
    }

    public String getSelectedAgentsJson() {
        return selectedAgentsJson;
    }

    public void setSelectedAgentsJson(String selectedAgentsJson) {
        this.selectedAgentsJson = selectedAgentsJson;
    }

    public String getFinalReportJson() {
        return finalReportJson;
    }

    public void setFinalReportJson(String finalReportJson) {
        this.finalReportJson = finalReportJson;
    }

    public String getReportsJson() {
        return reportsJson;
    }

    public void setReportsJson(String reportsJson) {
        this.reportsJson = reportsJson;
    }

    public String getReviewJson() {
        return reviewJson;
    }

    public void setReviewJson(String reviewJson) {
        this.reviewJson = reviewJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meeting)) return false;
        Meeting meeting = (Meeting) o;
        return Objects.equals(id, meeting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", sourceFilename='" + sourceFilename + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
