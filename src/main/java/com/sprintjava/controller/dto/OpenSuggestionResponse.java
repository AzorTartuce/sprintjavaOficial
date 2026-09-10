package com.sprintjava.controller.dto;

import com.sprintjava.model.Finding;
import com.sprintjava.model.Suggestion;

import java.time.LocalDateTime;

public class OpenSuggestionResponse {

    private Long id;
    private Long findingId;
    private String findingType;
    private Long triggeredByMeetingId;
    private String reason;
    private LocalDateTime createdAt;

    public OpenSuggestionResponse(Suggestion suggestion, Finding finding) {
        this.id = suggestion.getId();
        this.findingId = suggestion.getFindingId();
        this.findingType = finding != null ? finding.getFindingType() : null;
        this.triggeredByMeetingId = suggestion.getTriggeredByMeetingId();
        this.reason = suggestion.getReason();
        this.createdAt = suggestion.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getFindingId() {
        return findingId;
    }

    public String getFindingType() {
        return findingType;
    }

    public Long getTriggeredByMeetingId() {
        return triggeredByMeetingId;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
