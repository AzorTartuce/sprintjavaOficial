package com.sprintjava.model;

import java.time.LocalDateTime;
import java.util.Set;

public class Suggestion {

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_RECURRED = "recurred";

    public static final String DECISION_CONFIRM_RESOLVED = "confirm_resolved";
    public static final String DECISION_STILL_OPEN = "still_open";
    public static final String DECISION_RECURRED = "recurred";

    private static final Set<String> DECISOES_VALIDAS = Set.of(
            DECISION_CONFIRM_RESOLVED, DECISION_STILL_OPEN, DECISION_RECURRED);

    private Long id;
    private Long findingId;
    private Long triggeredByMeetingId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public Suggestion() {
    }

    public Suggestion(Long id, Long findingId, Long triggeredByMeetingId, String reason, String status,
                       LocalDateTime createdAt) {
        this.id = id;
        this.findingId = findingId;
        this.triggeredByMeetingId = triggeredByMeetingId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    /** Traduz a decisão do frontend (confirm_resolved/still_open/recurred) para o status persistido. */
    public String aplicarDecisao(String decisao) {
        if (decisao == null || !DECISOES_VALIDAS.contains(decisao)) {
            throw new IllegalArgumentException("Decisão inválida para sugestão: " + decisao);
        }
        this.status = switch (decisao) {
            case DECISION_CONFIRM_RESOLVED -> STATUS_ACCEPTED;
            case DECISION_STILL_OPEN -> STATUS_REJECTED;
            default -> STATUS_RECURRED;
        };
        return this.status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFindingId() {
        return findingId;
    }

    public void setFindingId(Long findingId) {
        this.findingId = findingId;
    }

    public Long getTriggeredByMeetingId() {
        return triggeredByMeetingId;
    }

    public void setTriggeredByMeetingId(Long triggeredByMeetingId) {
        this.triggeredByMeetingId = triggeredByMeetingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
