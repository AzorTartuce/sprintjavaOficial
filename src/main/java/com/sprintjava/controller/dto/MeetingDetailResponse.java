package com.sprintjava.controller.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintjava.model.Meeting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MeetingDetailResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Long id;
    private Long clientId;
    private String clientName;
    private String sourceFilename;
    private LocalDateTime createdAt;
    private String triage;
    private List<String> selectedAgents;
    private JsonNode finalReport;

    public MeetingDetailResponse(Meeting meeting, String clientName) {
        this.id = meeting.getId();
        this.clientId = meeting.getClientId();
        this.clientName = clientName;
        this.sourceFilename = meeting.getSourceFilename();
        this.createdAt = meeting.getCreatedAt();
        this.triage = meeting.getTriage();
        this.selectedAgents = parseAgentes(meeting.getSelectedAgentsJson());
        this.finalReport = parseJson(meeting.getFinalReportJson());
    }

    private static List<String> parseAgentes(String json) {
        try {
            List<String> agentes = new ArrayList<>();
            JsonNode node = OBJECT_MAPPER.readTree(json == null ? "[]" : json);
            node.forEach(item -> agentes.add(item.asText()));
            return agentes;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static JsonNode parseJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json == null ? "{}" : json);
        } catch (Exception e) {
            return OBJECT_MAPPER.createObjectNode();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getTriage() {
        return triage;
    }

    public List<String> getSelectedAgents() {
        return selectedAgents;
    }

    public JsonNode getFinalReport() {
        return finalReport;
    }
}
