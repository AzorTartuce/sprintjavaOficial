package com.sprintjava.controller.dto;

import com.sprintjava.model.Finding;
import com.sprintjava.model.FindingAttempt;

import java.time.LocalDateTime;

public class DashboardFindingRowResponse {

    private Long id;
    private Long clientId;
    private String findingType;
    private String title;
    private String severityHint;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private int attemptsCount;
    private String activeStrategy;

    public DashboardFindingRowResponse(Finding finding) {
        this.id = finding.getId();
        this.clientId = finding.getClientId();
        this.findingType = finding.getFindingType();
        this.title = finding.getTitle();
        this.severityHint = finding.getSeverityHint();
        this.status = finding.getStatus();
        this.openedAt = finding.getOpenedAt();
        this.closedAt = finding.getClosedAt();
        this.attemptsCount = finding.getAttempts().size();
        this.activeStrategy = finding.getAttempts().stream()
                .filter(a -> FindingAttempt.OUTCOME_IN_PROGRESS.equals(a.getOutcome()))
                .reduce((first, second) -> second)
                .map(FindingAttempt::getStrategyKey)
                .orElse(null);
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getFindingType() {
        return findingType;
    }

    public String getTitle() {
        return title;
    }

    public String getSeverityHint() {
        return severityHint;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public int getAttemptsCount() {
        return attemptsCount;
    }

    public String getActiveStrategy() {
        return activeStrategy;
    }
}
