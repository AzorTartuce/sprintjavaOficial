package com.sprintjava.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Finding {

    public static final String STATUS_AWAITING_STRATEGY = "awaiting_strategy";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_RESOLVED = "resolved";
    public static final String STATUS_DISMISSED = "dismissed";
    public static final String STATUS_RECURRED = "recurred";

    private static final Set<String> STATUS_VALIDOS = Set.of(
            STATUS_AWAITING_STRATEGY, STATUS_IN_PROGRESS, STATUS_RESOLVED, STATUS_DISMISSED, STATUS_RECURRED);

    private Long id;
    private Long clientId;
    private Long originMeetingId;
    private String findingType;
    private String title;
    private String severityHint;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private List<FindingAttempt> attempts = new ArrayList<>();

    public Finding() {
    }

    public Finding(Long clientId, Long originMeetingId, String findingType, String title, String severityHint) {
        this.clientId = clientId;
        this.originMeetingId = originMeetingId;
        this.findingType = findingType;
        this.title = title;
        this.severityHint = severityHint;
        this.status = STATUS_AWAITING_STRATEGY;
        this.openedAt = LocalDateTime.now();
    }

    public Finding(Long id, Long clientId, Long originMeetingId, String findingType, String title,
                   String severityHint, String status, LocalDateTime openedAt, LocalDateTime closedAt) {
        this.id = id;
        this.clientId = clientId;
        this.originMeetingId = originMeetingId;
        this.findingType = findingType;
        this.title = title;
        this.severityHint = severityHint;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    /** Aplica uma transição de status validando o vocabulário do frontend. */
    public void atualizarStatus(String novoStatus) {
        if (novoStatus == null || !STATUS_VALIDOS.contains(novoStatus)) {
            throw new IllegalArgumentException("Status de finding inválido: " + novoStatus);
        }
        this.status = novoStatus;
        if (STATUS_RESOLVED.equals(novoStatus) || STATUS_DISMISSED.equals(novoStatus)) {
            this.closedAt = LocalDateTime.now();
        } else {
            this.closedAt = null;
        }
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

    public Long getOriginMeetingId() {
        return originMeetingId;
    }

    public void setOriginMeetingId(Long originMeetingId) {
        this.originMeetingId = originMeetingId;
    }

    public String getFindingType() {
        return findingType;
    }

    public void setFindingType(String findingType) {
        this.findingType = findingType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeverityHint() {
        return severityHint;
    }

    public void setSeverityHint(String severityHint) {
        this.severityHint = severityHint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<FindingAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<FindingAttempt> attempts) {
        this.attempts = attempts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Finding)) return false;
        Finding finding = (Finding) o;
        return Objects.equals(id, finding.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
