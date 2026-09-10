package com.sprintjava.model;

import java.time.LocalDateTime;
import java.util.Set;

public class FindingAttempt {

    public static final String OUTCOME_IN_PROGRESS = "in_progress";
    public static final String OUTCOME_WORKED = "worked";
    public static final String OUTCOME_PARTIAL = "partial";
    public static final String OUTCOME_FAILED = "failed";

    private static final Set<String> OUTCOMES_VALIDOS = Set.of(
            OUTCOME_IN_PROGRESS, OUTCOME_WORKED, OUTCOME_PARTIAL, OUTCOME_FAILED);

    private Long id;
    private Long findingId;
    private String strategyKey;
    private String strategyLabel;
    private String notes;
    private String outcome;
    private LocalDateTime startedAt;
    private LocalDateTime outcomeAt;

    public FindingAttempt() {
    }

    public FindingAttempt(Long findingId, String strategyKey, String strategyLabel, String notes) {
        this.findingId = findingId;
        this.strategyKey = strategyKey;
        this.strategyLabel = strategyLabel;
        this.notes = notes;
        this.outcome = OUTCOME_IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public FindingAttempt(Long id, Long findingId, String strategyKey, String strategyLabel, String notes,
                           String outcome, LocalDateTime startedAt, LocalDateTime outcomeAt) {
        this.id = id;
        this.findingId = findingId;
        this.strategyKey = strategyKey;
        this.strategyLabel = strategyLabel;
        this.notes = notes;
        this.outcome = outcome;
        this.startedAt = startedAt;
        this.outcomeAt = outcomeAt;
    }

    /** Aplica um novo resultado à tentativa, validando o vocabulário do frontend. */
    public void registrarResultado(String novoOutcome, String notas) {
        if (novoOutcome == null || !OUTCOMES_VALIDOS.contains(novoOutcome)) {
            throw new IllegalArgumentException("Outcome de tentativa inválido: " + novoOutcome);
        }
        this.outcome = novoOutcome;
        if (notas != null) {
            this.notes = notas;
        }
        if (!OUTCOME_IN_PROGRESS.equals(novoOutcome)) {
            this.outcomeAt = LocalDateTime.now();
        }
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

    public String getStrategyKey() {
        return strategyKey;
    }

    public void setStrategyKey(String strategyKey) {
        this.strategyKey = strategyKey;
    }

    public String getStrategyLabel() {
        return strategyLabel;
    }

    public void setStrategyLabel(String strategyLabel) {
        this.strategyLabel = strategyLabel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getOutcomeAt() {
        return outcomeAt;
    }

    public void setOutcomeAt(LocalDateTime outcomeAt) {
        this.outcomeAt = outcomeAt;
    }
}
